package com.example.dontjusteat.notifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.dontjusteat.customer_my_notifications;
import com.example.dontjusteat.repositories.PreferencesRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

//Listens for new user notifications in Firestore and shows a local notification
public class NotificationsManager {
    private static final String TAG = "NotificationsManager";
    private static final String CHANNEL_ID = "user_updates";
    private final FirebaseFirestore db;
    private final Context context;
    private com.google.firebase.firestore.ListenerRegistration registration;
    private boolean isInitialLoad = true;

    public NotificationsManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        ensureChannel();

    }

    // start listening to /users/{uid}/notifications for new entries
    public void start() {

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) return;

        // get FCM token and save to Firestore
        retrieveAndSaveFcmToken(uid);

        registration = db.collection("users")
                .document(uid)
                .collection("notifications")
                .whereEqualTo("status", "unread")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) {
                        if (e != null || snapshots == null) return;

                        // on initial load, show all unread notifications
                        if (isInitialLoad) {
                            isInitialLoad = false;
                            // show each unread notification
                            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                                String title = doc.getString("title");
                                String desc = doc.getString("description");
                                String notifId = doc.getId();
                                if (shouldNotifyUser(title)) {
                                    showLocalNotification(
                                        title != null ? title : "Notification",
                                        desc != null ? desc : "",
                                        notifId
                                    );
                                }
                            }
                            return;
                        }

                        // after initial load, only show newly added notifications
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                String title = change.getDocument().getString("title");
                                String desc = change.getDocument().getString("description");
                                String notifId = change.getDocument().getId();
                                if (shouldNotifyUser(title)) {
                                    showLocalNotification(
                                        title != null ? title : "Notification",
                                        desc != null ? desc : "",
                                        notifId
                                    );
                                }
                            }
                        }
                    }
                });

    }

    public void stop() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
        isInitialLoad = true;

    }

    // get FCM token and save to user doc
    private void retrieveAndSaveFcmToken(String uid) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Log.d(TAG, "FCM token retrieved: " + token);
                    db.collection("users")
                            .document(uid)
                            .update("fcmToken", token)
                            .addOnSuccessListener(v -> Log.d(TAG, "FCM token saved"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to save token", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to get FCM token", e));
    }

    // simple heuristic until actual prefs are wired
    private boolean shouldNotifyUser(String title) {

        try {
            PreferencesRepository prefs = new PreferencesRepository(context);
            boolean isOffer = title != null && (title.toLowerCase().contains("offer") || title.toLowerCase().contains("discount"));
            boolean isSecondary = title != null && (title.toLowerCase().contains("booking") || title.toLowerCase().contains("update"));

            boolean allowOffers = true;      // later i will replace with prefs getter
            boolean allowSecondary = true;   // later i will replace with prefs getter

            if (isOffer) return allowOffers;
            if (isSecondary) return allowSecondary;
            return true;
        } catch (Exception e) {
            return true;
        }

    }

    @SuppressLint({"MissingPermission", "NotificationPermission"})
    private void showLocalNotification(String title, String message, String notificationId) {
        // create intent to open notifications page when user taps notification
        Intent intent = new Intent(context, customer_my_notifications.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        // runtime permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }


        try {
            NotificationManagerCompat.from(context).notify(notificationId.hashCode(), builder.build());
        } catch (SecurityException ignored) {
            //
        }
    }

    private void ensureChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "User Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Notifications based on user preferences");
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }
}

