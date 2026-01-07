package com.example.dontjusteat.models;

import com.google.firebase.Timestamp;

public class Booking {
    private String bookingId;
    private String restaurantId;
    private String userId;
    private String tableId;
    private Timestamp startTime;
    private Timestamp endTime;
    private int durationMinutes;
    private int partySize;
    private String status;  // "pending", "confirmed", "cancelled"
    private Timestamp createdAt;
    private boolean acknowledgedByStaff;  // when true, excludes from urgent

    public Booking() {
        // required for Firestore
    }

    public Booking(String restaurantId, String userId, String tableId, Timestamp startTime,
                   Timestamp endTime, int durationMinutes, int partySize, String status) {
        this.restaurantId = restaurantId;
        this.userId = userId;
        this.tableId = tableId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.partySize = partySize;
        this.status = status;
        this.createdAt = Timestamp.now();
    }

    // getters and setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getPartySize() { return partySize; }
    public void setPartySize(int partySize) { this.partySize = partySize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isAcknowledgedByStaff() { return acknowledgedByStaff; }
    public void setAcknowledgedByStaff(boolean acknowledged) { this.acknowledgedByStaff = acknowledged; }
}

