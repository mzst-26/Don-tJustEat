package com.example.dontjusteat.models;

import com.google.firebase.Timestamp;

public class Notification {
    private String id;
    private String title;
    private String description;
    private String status; // unread, read
    private Timestamp createdAt;
    private String bookingId;
    private String restaurantId;

    public Notification() {}

    public Notification(String title, String description, String status,
                        Timestamp createdAt, String bookingId, String restaurantId) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.bookingId = bookingId;
        this.restaurantId = restaurantId;
    }

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
}

