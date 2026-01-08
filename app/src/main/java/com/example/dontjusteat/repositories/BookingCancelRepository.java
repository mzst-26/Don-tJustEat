package com.example.dontjusteat.repositories;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class BookingCancelRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public void cancelBooking(@NonNull String restaurantId,
                              @NonNull String bookingId,
                              @NonNull String tableId,

                              @NonNull Timestamp startTime,
                              @NonNull Timestamp endTime,
                              @NonNull OnCancelListener listener) {


        // atomic update: status + release locks
        db.runTransaction((Transaction.Function<Void>) tx -> {
                    DocumentReference bookingRef = db.collection("restaurants").document(restaurantId)
                            .collection("bookings").document(bookingId);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "CANCELED");
                    updates.put("acknowledgedByStaff", false);
                    tx.update(bookingRef, updates);


                    // sync user side
                    String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
                    if (uid != null) {
                        DocumentReference userBookRef = db.collection("users").document(uid)
                                .collection("bookings").document(bookingId);
                        Map<String, Object> userUpdates = new HashMap<>();
                
                        userUpdates.put("status", "CANCELED");
                        userUpdates.put("acknowledgedByStaff", false);
                        tx.update(userBookRef, userUpdates);

                    }


                    // free up the time slots
                    releaseLocks(tx, restaurantId, tableId, startTime, endTime);
                    return null;
                }).addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    private void releaseLocks(Transaction tx, String restaurantId, String tableId, Timestamp startTime, Timestamp endTime) {
        long slotMs = TimeUnit.MINUTES.toMillis(15);
        long startMs = startTime.toDate().getTime();
        long endMs = endTime.toDate().getTime();

        // iterate 15 min slots and open them
        for (long t = startMs; t < endMs; t += slotMs) {
            String lockId = "slot_" + t;
            DocumentReference ref = db.collection("restaurants").document(restaurantId)
                    .collection("tables").document(tableId)
                    .collection("locks").document(lockId);
            Map<String, Object> data = new HashMap<>();
            data.put("status", "OPEN");
            tx.set(ref, data, SetOptions.merge());
        }
    }


    public interface OnCancelListener {
        void onSuccess();

        void onFailure(String error);
    }
}
