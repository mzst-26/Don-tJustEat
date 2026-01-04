package com.example.dontjusteat.repositories;

import android.content.Context;

import com.example.dontjusteat.models.UserPreferences;
import com.example.dontjusteat.security.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;


public class PreferencesRepository {
    // firebase instances
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final Context context;


    // Constructor
    public PreferencesRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.context = context;
    }

    // helper methods to get user type and get current user ID
    private String getCollectionName() {
        // check session for user type
        SessionManager sessionManager = new SessionManager(context);
        SessionManager.SessionData session = sessionManager.getSession();

        // return the right collection name
        if (session != null && session.isStaff) {
            return "admins";
        }
        return "users";
    }

    // Get current user id
    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    // Save the preferences in the database
    public void savePreferences(UserPreferences preferences, OnPreferencesActionListener listener) {
        String userId = getCurrentUserId();
        // check if user is authenticated
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }
        // get the right collection name
        String collection = getCollectionName();
        String preferencesDocId = userId + "_preferences";

        // save the preferences
        db.collection(collection).document(userId).collection("preferences").document(preferencesDocId)
                .set(preferences)
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // Load the preferences from the database
    public void loadPreferences(OnPreferencesLoadListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }
        // get the right collection name
        String collection = getCollectionName();
        String preferencesDocId = userId + "_preferences";

        // load the preferences
        db.collection(collection).document(userId).collection("preferences").document(preferencesDocId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Document exists, parse to UserPreferences object
                        UserPreferences preferences = document.toObject(UserPreferences.class);
                        if (preferences != null) {
                            // pass the object to the listener
                            listener.onSuccess(preferences);
                        } else {
                            // pass the error to the listener
                            listener.onFailure("Failed to parse preferences");
                        }
                    } else {
                        // f the document doesn't exist create default preferences
                        UserPreferences defaultPreferences = new UserPreferences(userId, true, true);
                        // save the default preferences
                        db.collection(collection).document(userId).collection("preferences").document(preferencesDocId)
                                .set(defaultPreferences)
                                .addOnSuccessListener(v -> listener.onSuccess(defaultPreferences))
                                .addOnFailureListener(e -> listener.onPreferencesNotFound());
                    }
                })
                .addOnFailureListener(e -> listener.onPreferencesNotFound());
    }

    // update the preferences in the database
    public void updateOffersAndDiscounts(boolean value, OnPreferencesActionListener listener) {
        String userId = getCurrentUserId();
        // check if user is authenticated
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }
        // get the right collection name
        String collection = getCollectionName();
        String preferencesDocId = userId + "_preferences";


        // update the preferences
        db.collection(collection).document(userId).collection("preferences").document(preferencesDocId)
                // update the offersAndDiscounts field
                .update("offersAndDiscounts", value, "lastUpdated", Timestamp.now())
                // call the relevant listener method
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }


    // update the preferences in the database
    public void updateSecondaryUpdates(boolean value, OnPreferencesActionListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }
        // get the right collection name
        String collection = getCollectionName();
        String preferencesDocId = userId + "_preferences";
        // update the preferences
        db.collection(collection).document(userId).collection("preferences").document(preferencesDocId)
                // update the secondaryUpdates field
                .update("secondaryUpdates", value, "lastUpdated", Timestamp.now())
                .addOnSuccessListener(v -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnPreferencesActionListener {
        // callback methods
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnPreferencesLoadListener {
        // callback methods
        void onSuccess(UserPreferences preferences);
        void onFailure(String error);
        void onPreferencesNotFound();
    }
}

