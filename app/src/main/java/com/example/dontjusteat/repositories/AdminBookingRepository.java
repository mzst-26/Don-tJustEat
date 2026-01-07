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
        public String customerPhone;
        public String tableId;
        public String time;
        public String date;
        public int guests;
        public String status;
        public Timestamp startTime;
        public boolean acknowledgedByStaff;
        public String restaurantId;

        public BookingModel(String bookingId, String customerName, String tableId, String time,
                            String date, int guests, String status, Timestamp startTime) {
            this.bookingId = bookingId;
            this.customerName = customerName;
            this.customerPhone = "";
            this.tableId = tableId;
            this.time = time;
            this.date = date;
            this.guests = guests;
            this.status = status;
            this.startTime = startTime;
            this.acknowledgedByStaff = false;
            this.restaurantId = "";
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
                            getCustomerName(userId, doc, restaurantId, urgent, today, listener, count, total);
                        } else {
                            BookingModel booking = mapToBookingModel(doc, "Guest", "", restaurantId);
                            if (booking != null) {
                                // exclude acknowledged bookings from urgent
                                if (!booking.acknowledgedByStaff && isUrgent(booking.status)) {
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

    private void getCustomerName(String userId, DocumentSnapshot bookingDoc, String restaurantId,
                                List<BookingModel> urgent, List<BookingModel> today,
                                OnBookingsLoadListener listener, int[] count, int total) {
        // get customer name and phone from users collection using userId
        db.collection("users").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                    String name = "Guest";
                    String phone = "";
                    if (userDoc.exists()) {
                        String userName = userDoc.getString("name");
                        if (userName != null && !userName.isEmpty()) {
                            name = userName;
                        }
                        String userPhone = userDoc.getString("phone");
                        if (userPhone != null) {
                            phone = userPhone;
                        }
                    }

                    BookingModel booking = mapToBookingModel(bookingDoc, name, phone, restaurantId);
                    if (booking != null) {
                        // exclude acknowledged bookings from urgent
                        if (!booking.acknowledgedByStaff && isUrgent(booking.status)) {
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
                    BookingModel booking = mapToBookingModel(bookingDoc, "Guest", "", restaurantId);
                    if (booking != null) {
                        // exclude acknowledged bookings from urgent
                        if (!booking.acknowledgedByStaff && isUrgent(booking.status)) {
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

    private BookingModel mapToBookingModel(DocumentSnapshot doc, String customerName, String customerPhone, String restaurantId) {
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
            Boolean acknowledged = doc.getBoolean("acknowledgedByStaff");

            if (startTs == null) return null;

            // format time and date
            String time = formatTime(startTs);
            String date = formatDate(startTs);

            BookingModel booking = new BookingModel(bookingId, customerName, tableId != null ? tableId : "", time, date, guests, status, startTs);
            booking.acknowledgedByStaff = acknowledged != null && acknowledged;
            booking.restaurantId = restaurantId;
            booking.customerPhone = customerPhone != null ? customerPhone : "";
            return booking;
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

    // update booking status in firestore
    public void updateBookingStatus(String restaurantId, String bookingId, String newStatus,
                                   boolean acknowledged, OnStatusUpdateListener listener) {
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", newStatus);
        if (acknowledged) {
            updates.put("acknowledgedByStaff", true);
        }

        // first get the booking to extract userId
        db.collection("restaurants").document(restaurantId)
                .collection("bookings").document(bookingId)
                .get()
                .addOnSuccessListener(bookingDoc -> {
                    if (!bookingDoc.exists()) {
                        listener.onFailure("Booking not found");
                        return;
                    }

                    String userId = bookingDoc.getString("userId");

                    // update restaurant booking
                    db.collection("restaurants").document(restaurantId)
                            .collection("bookings").document(bookingId)
                            .update(updates)
                            .addOnSuccessListener(v -> {
                                // also update user booking if userId exists
                                if (userId != null && !userId.isEmpty()) {
                                    db.collection("users").document(userId)
                                            .collection("bookings").document(bookingId)
                                            .update("status", newStatus)
                                            .addOnSuccessListener(v2 -> listener.onSuccess())
                                            .addOnFailureListener(e -> listener.onSuccess()); // still success even if user update fails
                                } else {
                                    listener.onSuccess();
                                }
                            })
                            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnStatusUpdateListener {
        void onSuccess();
        void onFailure(String error);
    }
}
