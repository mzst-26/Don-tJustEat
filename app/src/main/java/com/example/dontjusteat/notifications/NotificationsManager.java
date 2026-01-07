package com.example.dontjusteat.notifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.dontjusteat.repositories.PreferencesRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

//Listens for new user notifications in Firestore and shows a local notification
public class NotificationsManager {
    private static final String CHANNEL_ID = "user_updates";
    private final FirebaseFirestore db;
    private final Context context;
    private com.google.firebase.firestore.ListenerRegistration registration;

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

        registration = db.collection("users")
                .document(uid)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) {
                        if (e != null || snapshots == null) return;

                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.ADDED) {
                                String title = change.getDocument().getString("title");
                                String desc = change.getDocument().getString("description");
                                if (shouldNotifyUser(title)) {
                                    showLocalNotification(title != null ? title : "Notification",
                                            desc != null ? desc : "");
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
    private void showLocalNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);


        // runtime permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }


        try {
            NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
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

