package com.example.dontjusteat.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Restaurant {
    private String id;
    private String address;
    private int defaultDurationMinutes;
    private String imageUrl;
    private boolean isActive;
    private GeoPoint location;
    private String locationURL;
    private String name;
    private String phone;
    private int slotMinutes;


    public Restaurant() { }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }


    public String getAddress() { return address; }

    public int getDefaultDurationMinutes() { return defaultDurationMinutes; }

    public String getImageUrl() { return imageUrl; }

    public boolean isActive() { return isActive; }

    public GeoPoint getLocation() { return location; }

    public String getLocationURL() { return locationURL; }

    public String getName() { return name; }

    public String getPhone() { return phone; }

    public int getSlotMinutes() { return slotMinutes; }
}
