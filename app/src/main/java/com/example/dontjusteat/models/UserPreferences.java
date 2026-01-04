package com.example.dontjusteat.models;

import com.google.firebase.Timestamp;

public class UserPreferences {
    // fields
    private String userId;
    private boolean offersAndDiscounts;
    private boolean secondaryUpdates;
    private Timestamp lastUpdated;


    // Constructors
    public UserPreferences() {
        this.offersAndDiscounts = false;
        this.secondaryUpdates = false;
        this.lastUpdated = Timestamp.now();
    }

    // Constructor with parameters
    public UserPreferences(String userId, boolean offersAndDiscounts, boolean secondaryUpdates) {
        this.userId = userId;
        this.offersAndDiscounts = offersAndDiscounts;
        this.secondaryUpdates = secondaryUpdates;
        this.lastUpdated = Timestamp.now();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isOffersAndDiscounts() {
        return offersAndDiscounts;
    }

    public void setOffersAndDiscounts(boolean offersAndDiscounts) {
        this.offersAndDiscounts = offersAndDiscounts;
    }

    public boolean isSecondaryUpdates() {
        return secondaryUpdates;
    }

    public void setSecondaryUpdates(boolean secondaryUpdates) {
        this.secondaryUpdates = secondaryUpdates;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}

