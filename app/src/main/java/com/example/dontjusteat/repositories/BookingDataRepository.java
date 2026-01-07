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

    public interface OnBookingsLoaded {
        void onSuccess(List<BookingDisplayModel> bookings);
        void onFailure();
    }

    public static class BookingDisplayModel {
        public final String locationName;
        public final String address;
        public final String date;
        public final String time;
        public final String guests;
        public final String status;
        public final String bookingId;
        public final long sortTimestamp;

        public BookingDisplayModel(String locationName, String address, String date, String time,
                                   String guests, String status, String bookingId, long sortTimestamp) {
            this.locationName = locationName;
            this.address = address;
            this.date = date;
            this.time = time;
            this.guests = guests;
            this.status = status;
            this.bookingId = bookingId;
            this.sortTimestamp = sortTimestamp;
        }

    }

    public void fetchUserBookings(OnBookingsLoaded callback) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            callback.onSuccess(new ArrayList<>());
            return;
        }



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
                            }else {
                                //
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> callback.onFailure());
    }

    private interface OnHydrated {
        void done(BookingDisplayModel model);

    }

    private void hydrateBooking(DocumentSnapshot userBookingDoc, OnHydrated onHydrated, Runnable onFail) {
        String restaurantId = userBookingDoc.getString("restaurantId");
        String bookingId = userBookingDoc.getString("bookingId");
        Timestamp startTimeTs = userBookingDoc.getTimestamp("startTime");
        Timestamp createdAtTs = userBookingDoc.getTimestamp("createdAt");
        String status = userBookingDoc.getString("status");

        if (restaurantId == null || bookingId == null) {
            onHydrated.done(null);
            return;
        }

        // fetch booking and restaurant docs in parallel then map
        Tasks.whenAllSuccess(
                db.collection("restaurants").document(restaurantId).collection("bookings").document(bookingId).get(),
                db.collection("restaurants").document(restaurantId).get()
        ).addOnSuccessListener(results -> {
            DocumentSnapshot restBookingDoc = results.size() > 0 ? (DocumentSnapshot) results.get(0) : null;
            DocumentSnapshot restaurantDoc = results.size() > 1 ? (DocumentSnapshot) results.get(1) : null;
            BookingDisplayModel model = mapToModel(userBookingDoc, restBookingDoc, restaurantDoc, restaurantId, bookingId, startTimeTs, createdAtTs, status);
            onHydrated.done(model);
        }).addOnFailureListener(e -> onFail.run());
    }

    private BookingDisplayModel mapToModel(DocumentSnapshot userBookingDoc, DocumentSnapshot restBookingDoc, DocumentSnapshot restaurantDoc,
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
        if (locationName == null || locationName.isEmpty()) locationName = restaurantId != null ? restaurantId : "";

        String locationAddress = restaurantDoc != null ? restaurantDoc.getString("address") : null;
        if (locationAddress == null || locationAddress.isEmpty()) {
            locationAddress = restBookingDoc != null ? restBookingDoc.getString("restaurantAddress") : "";
        }
        if (locationAddress == null) locationAddress = "";

        String statusVal = status != null ? status : restBookingDoc != null ? restBookingDoc.getString("status") : "";


        return new BookingDisplayModel(
                locationName,
                locationAddress,
                date,
                time,
                guests,
                statusVal != null ? statusVal : "",
                bookingId != null ? bookingId : "",
                sortTs

        );
    }
}
