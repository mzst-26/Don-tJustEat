package com.example.dontjusteat.models;


import com.google.firebase.Timestamp;

public class User {
    private String uid;
    private String email;
    private String name;
    private String phone;
    private final Timestamp createdAt;
    private boolean isRoleCustomer;

    private String photoUrl;

    private boolean isActive;

    // add isVerified flag for email verification status
    private boolean isVerified;

    // no-arg constructor for firebase
    public User() {
        createdAt = Timestamp.now();
    }

    public User(String uid, String email, String name, String phone, Timestamp createdAt, boolean isRoleCustomer, boolean isActive, String photoUrl) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.createdAt = createdAt;
        this.isRoleCustomer = isRoleCustomer;
        this.isActive = isActive;
        this.photoUrl = photoUrl;
        // default to false until email is verified
        this.isVerified = false;


    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isRoleCustomer() {
        return isRoleCustomer;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(boolean status) {
        isActive = status;
    }

    public void setRoleCustomer(boolean isRoleCustomer) {
        this.isRoleCustomer = isRoleCustomer;
    }

    // isVerified getter/setter
    public boolean getIsVerified() { return isVerified; }
    public void setIsVerified(boolean verified) { this.isVerified = verified; }
}
