package com.example.dontjusteat.repositories;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;



public class AdminBookingRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static class BookingModel {
        public String bookingId;
        public String customerName;
        public String tableId;
        public String time;
        public String date;
        public int guests;
        public String status;
        public Timestamp startTime;

        public BookingModel(String bookingId, String customerName, String tableId, String time,
                            String date, int guests, String status, Timestamp startTime) {
            this.bookingId = bookingId;
            this.customerName = customerName;
            this.tableId = tableId;
            this.time = time;
            this.date = date;
            this.guests = guests;
            this.status = status;
            this.startTime = startTime;
        }
    }

    public interface OnBookingsLoadListener {
        void onUrgentLoaded(List<BookingModel> urgent);
        void onTodayLoaded(List<BookingModel> today);
        void onFailure(String error);
    }

    // fetch admin's locationId and load bookings
    public void loadAdminBookings(OnBookingsLoadListener listener) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            listener.onFailure("Not authenticated");
            return;
        }

        // get admin doc and extract their restaurant location
        db.collection("admins").document(uid).get()
                .addOnSuccessListener(adminDoc -> {
                    if (adminDoc == null || !adminDoc.exists()) {
                        listener.onFailure("Admin not found");
                        return;
                    }
                    String locationId = adminDoc.getString("locationId");
                    if (locationId == null || locationId.isEmpty()) {
                        listener.onFailure("Location not set");
                        return;
                    }
                    // now fetch all bookings for this location
                    fetchBookingsForRestaurant(locationId, listener);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    private void fetchBookingsForRestaurant(String restaurantId, OnBookingsLoadListener listener) {
        db.collection("restaurants").document(restaurantId)
                .collection("bookings")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<BookingModel> urgent = new ArrayList<>();
                    List<BookingModel> today = new ArrayList<>();

                    int total = snapshot.getDocuments().size();
                    if (total == 0) {
                        listener.onUrgentLoaded(urgent);
                        listener.onTodayLoaded(today);
                        return;
                    }

                    // load all bookings and get customer names
                    int[] count = {0};
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String userId = doc.getString("userId");
                        if (userId != null && !userId.isEmpty()) {
                            // fetch customer name from users collection
                            getCustomerName(userId, doc, urgent, today, listener, count, total);
                        } else {
                            BookingModel booking = mapToBookingModel(doc, "Guest");
                            if (booking != null) {
                                if (isUrgent(booking.status)) {
                                    urgent.add(booking);
                                } else if (isToday(booking.startTime)) {
                                    today.add(booking);
                                }
                            }
                            count[0]++;
                            if (count[0] == total) {
                                listener.onUrgentLoaded(urgent);
                                listener.onTodayLoaded(today);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    private void getCustomerName(String userId, DocumentSnapshot bookingDoc,
                                List<BookingModel> urgent, List<BookingModel> today,
                                OnBookingsLoadListener listener, int[] count, int total) {
        // get customer name from users collection using userId
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    String name = "Guest";
                    if (userDoc.exists()) {
                        String userName = userDoc.getString("name");
                        if (userName != null && !userName.isEmpty()) {
                            name = userName;
                        }
                    }

                    BookingModel booking = mapToBookingModel(bookingDoc, name);
                    if (booking != null) {
                        if (isUrgent(booking.status)) {
                            urgent.add(booking);
                        } else if (isToday(booking.startTime)) {
                            today.add(booking);
                        }
                    }

                    count[0]++;
                    if (count[0] == total) {
                        listener.onUrgentLoaded(urgent);
                        listener.onTodayLoaded(today);
                    }
                })
                .addOnFailureListener(e -> {
                    // if fetch fails just use guest
                    BookingModel booking = mapToBookingModel(bookingDoc, "Guest");
                    if (booking != null) {
                        if (isUrgent(booking.status)) {
                            urgent.add(booking);
                        } else if (isToday(booking.startTime)) {
                            today.add(booking);
                        }
                    }

                    count[0]++;
                    if (count[0] == total) {
                        listener.onUrgentLoaded(urgent);
                        listener.onTodayLoaded(today);
                    }
                });
    }

    private BookingModel mapToBookingModel(DocumentSnapshot doc, String customerName) {
        try {
            String bookingId = doc.getId();
            if (customerName == null || customerName.isEmpty()) {
                customerName = "Guest";
            }

            // get table and party size
            String tableId = doc.getString("tableId");
            Long guestsLong = doc.getLong("partySize");
            int guests = guestsLong != null ? guestsLong.intValue() : 0;
            String status = doc.getString("status");
            Timestamp startTs = doc.getTimestamp("startTime");

            if (startTs == null) return null;

            // format time and date
            String time = formatTime(startTs);
            String date = formatDate(startTs);

            return new BookingModel(bookingId, customerName, tableId != null ? tableId : "", time, date, guests, status, startTs);
        } catch (Exception e) {
            return null;
        }
    }

    // check if booking needs urgent action
    private boolean isUrgent(String status) {
        if (status == null) return false;
        // check if status is canceled, change request, or pending
        return status.equalsIgnoreCase("CANCELED") ||
               status.equalsIgnoreCase("CHANGE REQUEST") ||
               status.equalsIgnoreCase("PENDING");
    }

    // check if booking is happening today (london tz)
    private boolean isToday(Timestamp ts) {
        if (ts == null) return false;
        // get today's date
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
        Calendar bookingCal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
        bookingCal.setTime(ts.toDate());

        // compare years and days
        return now.get(Calendar.YEAR) == bookingCal.get(Calendar.YEAR) &&
               now.get(Calendar.DAY_OF_YEAR) == bookingCal.get(Calendar.DAY_OF_YEAR);
    }


    private String formatTime(Timestamp ts) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ts.toDate());
        // get hour and minute
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        return String.format("%02d:%02d", hour, minute);
    }



    private String formatDate(Timestamp ts) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(ts.toDate());
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        // get day, month, year
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        // format and return

        return String.format("%s %d, %d", months[month], day, year);
    }
}
