package com.example.dontjusteat;

import android.app.Application;
import android.util.Log;

import com.example.dontjusteat.notifications.NotificationsManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class DontJustEatApplication extends Application {
    private static final String TAG = "DontJustEatApp";
    private NotificationsManager notificationsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized successfully");
            } else {
                Log.d(TAG, "Firebase already initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase", e);
        }

        // start notifications listener if user already logged in
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                notificationsManager = new NotificationsManager(this);
                notificationsManager.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start notifications listener", e);
        }
    }
}
