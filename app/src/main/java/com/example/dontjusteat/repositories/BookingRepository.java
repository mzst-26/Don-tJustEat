package com.example.dontjusteat.repositories;

import com.example.dontjusteat.models.Booking;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class BookingRepository {
    private final FirebaseFirestore db;

    public BookingRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // create booking with transaction to prevent double-booking
    public void createBooking(String restaurantId, String userId, String tableId,
                              Timestamp startTime, int durationMinutes, int partySize,
                              OnBookingCreateListener listener) {

        // validate inputs
        if (restaurantId == null || restaurantId.isEmpty() ||
            userId == null || userId.isEmpty() ||
            tableId == null || tableId.isEmpty() ||
            startTime == null || durationMinutes <= 0 || partySize <= 0) {
            listener.onFailure("Invalid booking data");
            return;
        }

        // compute end time
        long endMs = startTime.toDate().getTime() + (durationMinutes * 60 * 1000L);
        Timestamp endTime = new Timestamp(endMs / 1000, (int)((endMs % 1000) * 1000000));

        // check for overlapping bookings first
        checkForOverlappingBookings(restaurantId, tableId, startTime, endTime)
                .addOnSuccessListener(hasOverlap -> {
                    if (hasOverlap) {
                        listener.onFailure("Table is already booked for this time");
                        return;
                    }

                    // create booking if no overlap
                    createBookingInFirestore(restaurantId, userId, tableId, startTime, endTime,
                            durationMinutes, partySize, listener);
                })
                .addOnFailureListener(e -> listener.onFailure("Failed to check availability: " + e.getMessage()));
    }

    // check for overlapping bookings
    private Task<Boolean> checkForOverlappingBookings(String restaurantId, String tableId,
                                                      Timestamp startTime, Timestamp endTime) {
        TaskCompletionSource<Boolean> tcs = new TaskCompletionSource<>();

        // fetch all bookings for this table and check overlap in code
        db.collection("restaurants").document(restaurantId)
                .collection("bookings")
                .whereEqualTo("tableId", tableId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    // check if any booking overlaps with our time window
                    boolean hasOverlap = false;
                    long requestedStart = startTime.toDate().getTime();
                    long requestedEnd = endTime.toDate().getTime();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        Timestamp bookingStart = doc.getTimestamp("startTime");
                        Timestamp bookingEnd = doc.getTimestamp("endTime");

                        if (bookingStart != null && bookingEnd != null) {
                            long existingStart = bookingStart.toDate().getTime();
                            long existingEnd = bookingEnd.toDate().getTime();

                            // check if time windows overlap
                            if (existingStart < requestedEnd && existingEnd > requestedStart) {
                                hasOverlap = true;
                                break;
                            }
                        }
                    }

                    tcs.setResult(hasOverlap);
                })
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }

    // create booking in Firestore using batch write
    private void createBookingInFirestore(String restaurantId, String userId, String tableId,
                                          Timestamp startTime, Timestamp endTime,
                                          int durationMinutes, int partySize,
                                          OnBookingCreateListener listener) {

        // create booking object
        Booking booking = new Booking(restaurantId, userId, tableId, startTime, endTime,
                durationMinutes, partySize, "pending");

        // generate booking ID
        DocumentReference bookingRef = db.collection("restaurants")
                .document(restaurantId)
                .collection("bookings")
                .document();

        String bookingId = bookingRef.getId();
        booking.setBookingId(bookingId);

        // use batch write for atomicity
        WriteBatch batch = db.batch();

        // write to restaurant bookings
        batch.set(bookingRef, booking);

        // write reference to user bookings
        Map<String, Object> userBookingRef = new HashMap<>();
        userBookingRef.put("bookingId", bookingId);
        userBookingRef.put("restaurantId", restaurantId);
        userBookingRef.put("startTime", startTime);
        userBookingRef.put("status", "pending");
        userBookingRef.put("createdAt", Timestamp.now());

        DocumentReference userBookingDoc = db.collection("users")
                .document(userId)
                .collection("bookings")
                .document(bookingId);

        batch.set(userBookingDoc, userBookingRef);

        // mark time slots as HELD in the locks subcollection
        markSlotsAsHeld(batch, restaurantId, tableId, startTime, endTime, durationMinutes);

        // commit batch
        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onSuccess(bookingId))
                .addOnFailureListener(e -> listener.onFailure("Failed to create booking: " + e.getMessage()));
    }

    // mark time slots as HELD in locks subcollection
    private void markSlotsAsHeld(WriteBatch batch, String restaurantId, String tableId,
                                 Timestamp startTime, Timestamp endTime, int durationMinutes) {
        // calculate all 15-minute slots that need to be marked as held
        long startMs = startTime.toDate().getTime();
        long endMs = endTime.toDate().getTime();
        long slotIntervalMs = 15 * 60 * 1000L; // 15 minutes in milliseconds

        // iterate through each 15-min slot in the booking window
        for (long slotMs = startMs; slotMs < endMs; slotMs += slotIntervalMs) {
            Timestamp slotStartTime = new Timestamp(slotMs / 1000, (int)((slotMs % 1000) * 1000000));

            // create lock document
            Map<String, Object> lockData = new HashMap<>();
            lockData.put("startTime", slotStartTime);
            lockData.put("status", "held");

            // generate lock document ID based on timestamp for uniqueness
            String lockId = "slot_" + slotMs;

            DocumentReference lockRef = db.collection("restaurants")
                    .document(restaurantId)
                    .collection("tables")
                    .document(tableId)
                    .collection("locks")
                    .document(lockId);

            batch.set(lockRef, lockData);
        }
    }

    // listener interface
    public interface OnBookingCreateListener {
        void onSuccess(String bookingId);
        void onFailure(String error);
    }
}

