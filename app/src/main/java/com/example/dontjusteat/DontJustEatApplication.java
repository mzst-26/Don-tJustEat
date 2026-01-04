package com.example.dontjusteat;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class DontJustEatApplication extends Application {
    private static final String TAG = "DontJustEatApp";

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
    }
}

