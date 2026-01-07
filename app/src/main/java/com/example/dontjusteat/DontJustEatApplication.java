package com.example.dontjusteat;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

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

        // start notifications listener if user logged in and permission granted
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null && hasNotificationPermission()) {
                notificationsManager = new NotificationsManager(this);
                notificationsManager.start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start notifications listener", e);
        }
    }

    // check if notification permission is granted
    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
