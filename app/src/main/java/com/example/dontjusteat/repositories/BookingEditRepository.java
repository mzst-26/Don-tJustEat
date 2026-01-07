package com.example.dontjusteat.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.SetOptions;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// handles booking change requests: validate availability, swap locks, update statuses atomically
public class BookingEditRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public interface OnRequestEditListener {
        void onSuccess();
        void onFailure(String error);
    }

    // reusing overlapping booking check
    private Task<Boolean> hasOverlap(String restaurantId, String tableId, Timestamp startTime, Timestamp endTime, String excludeBookingId) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();


        db.collection("restaurants").document(restaurantId)
                .collection("bookings")
                .whereEqualTo("tableId", tableId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    boolean overlap = false;
                    long reqStart = startTime.toDate().getTime();
                    long reqEnd = endTime.toDate().getTime();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        if (excludeBookingId != null && excludeBookingId.equals(doc.getId())) continue;
                        // get the start and end times of the booking
                        Timestamp s = doc.getTimestamp("startTime");
                        Timestamp e = doc.getTimestamp("endTime");
                        // if they exist
                        if (s != null && e != null) {
                            long es = s.toDate().getTime();
                            long ee = e.toDate().getTime();
                            if (es < reqEnd && ee > reqStart) {
                                overlap = true; break;
                            }
                        }
                    }
                    // return the result
                    tcs.setResult(overlap);
                })
                .addOnFailureListener(tcs::setException);
        return tcs.getTask();
    }

    // release old locks to OPEN, reserve new locks as held
    private void swapLocks(Transaction tx, String restaurantId, String tableId, Timestamp oldStart, Timestamp oldEnd, Timestamp newStart, Timestamp newEnd) {
        long slotMs = TimeUnit.MINUTES.toMillis(15);

        // release old time window
        long oldS = oldStart.toDate().getTime();
        long oldE = oldEnd.toDate().getTime();
        for (long t = oldS; t < oldE; t += slotMs) {
            String lockId = "slot_" + t;
            DocumentReference ref = db.collection("restaurants").document(restaurantId)
                    .collection("tables").document(tableId)
                    .collection("locks").document(lockId);
            Map<String,Object> data = new HashMap<>();
            data.put("status", "OPEN");
            tx.set(ref, data, SetOptions.merge());
        }


        // reserve new time window
        long newS = newStart.toDate().getTime();
        long newE = newEnd.toDate().getTime();
        for (long t = newS; t < newE; t += slotMs) {
            String lockId = "slot_" + t;
            DocumentReference ref = db.collection("restaurants").document(restaurantId)
                    .collection("tables").document(tableId)
                    .collection("locks").document(lockId);
            Map<String,Object> data = new HashMap<>();
            data.put("startTime", new Timestamp(t/1000, (int)((t%1000)*1000000)));
            data.put("status", "held");
            tx.set(ref, data, SetOptions.merge());
        }
    }

    // Entry point: request an edit for date/time/guests; validates then performs atomic updates
    public void requestEdit(@NonNull String restaurantId,
                            @NonNull String bookingId,
                            @NonNull String tableId,
                            @NonNull Timestamp currentStart,
                            int durationMinutes,
                            @NonNull Timestamp newStart,
                            int newGuests,
                            @NonNull OnRequestEditListener listener) {
        if (durationMinutes <= 0) { listener.onFailure("Invalid duration"); return; }

        long newEndMs = newStart.toDate().getTime() + TimeUnit.MINUTES.toMillis(durationMinutes);
        Timestamp newEnd = new Timestamp(newEndMs/1000, (int)((newEndMs%1000)*1000000));


        //no overlapping booking for same table and locks window assumed by locks
        hasOverlap(restaurantId, tableId, newStart, newEnd, bookingId)
                .addOnSuccessListener(overlap -> {
                    if (overlap) { listener.onFailure("Selected time is not available"); return; }


                    // Transaction: update booking status, time, partySize (if allowed), and swap locks
                    db.runTransaction((Transaction.Function<Void>) tx -> {
                        DocumentReference bookingRef = db.collection("restaurants").document(restaurantId)
                                .collection("bookings").document(bookingId);
                        DocumentSnapshot bookingSnap = tx.get(bookingRef);
                        if (!bookingSnap.exists()) throw new IllegalStateException("Booking not found");

                        Timestamp oldStart = bookingSnap.getTimestamp("startTime");
                        Timestamp oldEnd = bookingSnap.getTimestamp("endTime");
                        if (oldStart == null || oldEnd == null) throw new IllegalStateException("Invalid booking times");


                        // validate locks are OPEN for all required slots before writing
                        long slotMs = TimeUnit.MINUTES.toMillis(15);
                        long newS = newStart.toDate().getTime();
                        long newE = newEnd.toDate().getTime();



                        for (long t = newS; t < newE; t += slotMs) {
                            String lockId = "slot_" + t;
                            DocumentReference ref = db.collection("restaurants").document(restaurantId)
                                    .collection("tables").document(tableId)
                                    .collection("locks").document(lockId);
                            DocumentSnapshot lockSnap = tx.get(ref);
                            String status = lockSnap.exists() ? lockSnap.getString("status") : "OPEN";
                            if (status != null && !status.equalsIgnoreCase("OPEN")) {
                                throw new IllegalStateException("Selected time just became unavailable");
                            }
                        }



                        Map<String,Object> updates = new HashMap<>();
                        updates.put("status", "CHANGE REQUEST");
                        updates.put("startTime", newStart);
                        updates.put("endTime", newEnd);
                        updates.put("partySize", newGuests);
                        tx.update(bookingRef, updates);

                        // also mirror status under user booking if exists
                        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
                        if (uid != null) {
                            DocumentReference userBookRef = db.collection("users").document(uid)
                                    .collection("bookings").document(bookingId);
                            tx.update(userBookRef, "status", "CHANGE REQUEST", "startTime", newStart);
                        }

                        // swap locks atomically
                        swapLocks(tx, restaurantId, tableId, oldStart, oldEnd, newStart, newEnd);
                        return null;
                    }).addOnSuccessListener(aVoid -> listener.onSuccess())
                      .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure("Failed to validate: " + e.getMessage()));
    }
}
