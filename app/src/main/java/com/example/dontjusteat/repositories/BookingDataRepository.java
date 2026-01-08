package com.example.dontjusteat.repositories;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

// Fetches user bookings and hydrates with restaurant booking details
public class BookingDataRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    //load user's bookings from Firestore and hydrate with restaurant details
    public void fetchUserBookings(OnBookingsLoaded callback) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // fetch user bookings ordered by creation time
        db.collection("users")
                .document(uid)
                .collection("bookings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(25)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || snapshot.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<BookingDisplayModel> list = new ArrayList<>();

                    // track completion to call callback once all hydrations finish
                    AtomicInteger completed = new AtomicInteger(0);
                    int total = snapshot.size();


                    for (DocumentSnapshot userDoc : snapshot.getDocuments()) {
                        hydrateBooking(userDoc, model -> {
                            if (model != null) list.add(model);
                            if (completed.incrementAndGet() == total) {
                                callback.onSuccess(list);
                            }
                        }, () -> {
                            if (completed.incrementAndGet() == total) {
                                callback.onSuccess(list);
                            } else {
                                //
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> callback.onFailure());
    }

    // fetch booking and restaurant docs in parallel, then map to display model
    private void hydrateBooking(DocumentSnapshot userBookingDoc, OnHydrated onHydrated, Runnable onFail) {
        String restaurantId = userBookingDoc.getString("restaurantId");
        String bookingId = userBookingDoc.getString("bookingId");
        Timestamp startTimeTs = userBookingDoc.getTimestamp("startTime");
        Timestamp createdAtTs = userBookingDoc.getTimestamp("createdAt");
        String status = userBookingDoc.getString("status");
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (restaurantId == null || bookingId == null) {
            onHydrated.done(null);
            return;
        }

        // parallel fetch: booking details + restaurant info + user profile
        Tasks.whenAllSuccess(
                db.collection("restaurants").document(restaurantId).collection("bookings").document(bookingId).get(),
                db.collection("restaurants").document(restaurantId).get(),
                userId != null ? db.collection("users").document(userId).get() : Tasks.forResult(null)
        ).addOnSuccessListener(results -> {
            DocumentSnapshot restBookingDoc = results.size() > 0 ? (DocumentSnapshot) results.get(0) : null;
            DocumentSnapshot restaurantDoc = results.size() > 1 ? (DocumentSnapshot) results.get(1) : null;
            DocumentSnapshot userDoc = results.size() > 2 ? (DocumentSnapshot) results.get(2) : null;
            BookingDisplayModel model = mapToModel(userBookingDoc, restBookingDoc, restaurantDoc, userDoc, restaurantId, bookingId, startTimeTs, createdAtTs, status);
            onHydrated.done(model);
        }).addOnFailureListener(e -> onFail.run());
    }

    private BookingDisplayModel mapToModel(DocumentSnapshot userBookingDoc, DocumentSnapshot restBookingDoc, DocumentSnapshot restaurantDoc, DocumentSnapshot userDoc,
                                           String restaurantId, String bookingId, Timestamp startTimeTs,
                                           Timestamp createdAtTs, String status) {
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.UK);


        Timestamp startTs = startTimeTs != null ? startTimeTs : restBookingDoc != null ? restBookingDoc.getTimestamp("startTime") : null;
        long sortTs = startTs != null ? startTs.toDate().getTime() : (createdAtTs != null ? createdAtTs.toDate().getTime() : System.currentTimeMillis());

        String date = startTs != null ? dateFmt.format(startTs.toDate()) : "";
        String time = startTs != null ? timeFmt.format(startTs.toDate()) : "";

        Long partySize = restBookingDoc != null ? restBookingDoc.getLong("partySize") : null;
        String guests = partySize != null ? partySize + " Guests" : "";

        // prefer restaurant doc for name/address
        String locationName = restaurantDoc != null ? restaurantDoc.getString("name") : null;
        if (locationName == null || locationName.isEmpty()) {
            locationName = restBookingDoc != null ? restBookingDoc.getString("restaurantName") : null;
        }
        if (locationName == null || locationName.isEmpty())
            locationName = restaurantId != null ? restaurantId : "";

        String locationAddress = restaurantDoc != null ? restaurantDoc.getString("address") : null;
        if (locationAddress == null || locationAddress.isEmpty()) {
            locationAddress = restBookingDoc != null ? restBookingDoc.getString("restaurantAddress") : "";
        }
        if (locationAddress == null) locationAddress = "";

        String statusVal = status != null ? status : restBookingDoc != null ? restBookingDoc.getString("status") : "";

        // extract booking details for edit requests
        String tableId = restBookingDoc != null ? restBookingDoc.getString("tableId") : null;
        Long durationLong = restBookingDoc != null ? restBookingDoc.getLong("durationMinutes") : null;
        int durationMinutes = durationLong != null ? durationLong.intValue() : 90;

        // extract restaurant image URL
        String restaurantImageUrl = restaurantDoc != null ? restaurantDoc.getString("imageUrl") : null;
        if (restaurantImageUrl == null || restaurantImageUrl.isEmpty()) {
            restaurantImageUrl = "";
        }

        // extract user profile URL from user document (users/{userId})
        String userProfileUrl = userDoc != null ? userDoc.getString("photoUrl") : null;
        if (userProfileUrl == null || userProfileUrl.isEmpty()) {
            userProfileUrl = "";
        }

        return new BookingDisplayModel(
                locationName,
                locationAddress,
                date,
                time,
                guests,
                statusVal != null ? statusVal : "",
                bookingId != null ? bookingId : "",
                sortTs,
                restaurantId,
                tableId != null ? tableId : "",
                startTs,
                durationMinutes,
                restaurantImageUrl,
                userProfileUrl
        );
    }

    public interface OnBookingsLoaded {
        void onSuccess(List<BookingDisplayModel> bookings);

        void onFailure();
    }

    private interface OnHydrated {
        void done(BookingDisplayModel model);

    }

    /**
     * @param restaurantId       add fields needed for edit
     * @param restaurantImageUrl image URLs
     */
    public record BookingDisplayModel(String locationName, String address, String date, String time,
                                      String guests, String status, String bookingId,
                                      long sortTimestamp, String restaurantId, String tableId,
                                      Timestamp startTime, int durationMinutes,
                                      String restaurantImageUrl, String userProfileUrl) {
    }
}
