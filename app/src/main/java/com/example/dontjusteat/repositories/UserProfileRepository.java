package com.example.dontjusteat.repositories;

import android.content.Context;

import com.example.dontjusteat.security.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context context;

    public UserProfileRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.context = context;
    }

    public void updateUserName(String name, OnProfileUpdateListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        String collection = getCollectionName();
        db.collection(collection).document(userId).update("name", name)
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateUserPhone(String phone, OnProfileUpdateListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        String collection = getCollectionName();
        db.collection(collection).document(userId).update("phone", phone)
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void updateUserPhoto(String photoUrl, OnProfileUpdateListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        String collection = getCollectionName();
        db.collection(collection).document(userId).update("photoUrl", photoUrl)
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void loadUserProfile(OnProfileLoadListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        String collection = getCollectionName();
        db.collection(collection).document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("name");
                        String phone = document.getString("phone");
                        String photoUrl = document.getString("photoUrl");
                        listener.onSuccess(name, phone, photoUrl);
                    } else {
                        listener.onFailure("User profile not found");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    private String getCollectionName() {
        SessionManager sessionManager = new SessionManager(context);
        SessionManager.SessionData session = sessionManager.getSession();

        if (session != null && session.isStaff) {
            return "admins";
        }
        return "users";
    }


    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public interface OnProfileUpdateListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnProfileLoadListener {
        void onSuccess(String name, String phone, String photoUrl);
        void onFailure(String error);
    }
}

