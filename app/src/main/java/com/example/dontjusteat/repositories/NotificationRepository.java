package com.example.dontjusteat.repositories;

import android.content.Context;

import com.example.dontjusteat.models.Notification;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationRepository {
    private final FirebaseFirestore db;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<String> createNotification(String userId, Notification notification) {
        TaskCompletionSource<String> tcs = new TaskCompletionSource<>();
        // validate inputs
        if (userId == null || userId.isEmpty()) {
            tcs.setException(new IllegalArgumentException("User ID required"));
            return tcs.getTask();
        }
        // validate inputs
        if (notification == null) {
            tcs.setException(new IllegalArgumentException("Notification data required"));
            return tcs.getTask();
        }


        // set defaults
        if (notification.getStatus() == null) notification.setStatus("unread");
        if (notification.getCreatedAt() == null) notification.setCreatedAt(Timestamp.now());
        //
        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("notifications")
                .document();
        // save the notification
        String notificationId = docRef.getId();
        notification.setId(notificationId);
        // save the notification
        docRef.set(notification)
                .addOnSuccessListener(aVoid -> tcs.setResult(notificationId))
                .addOnFailureListener(tcs::setException);

        return tcs.getTask();
    }
    // update notification status
    public interface OnNotificationListener {
        void onSuccess(String notificationId);
        void onFailure(String error);
    }
}

