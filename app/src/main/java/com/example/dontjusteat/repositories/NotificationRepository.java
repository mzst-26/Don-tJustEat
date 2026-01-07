package com.example.dontjusteat.repositories;

import android.content.Context;

import com.example.dontjusteat.models.Notification;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

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
    public void updateNotificationStatus(String userId, String notificationId, String status, OnNotificationListener listener) {
        if (userId == null || userId.isEmpty() || notificationId == null || notificationId.isEmpty()) {
            if (listener != null) listener.onFailure("Missing IDs");
            return;
        }
        // validate inputs
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("status", status)
                .addOnSuccessListener(v -> { if (listener != null) listener.onSuccess(notificationId); })
                .addOnFailureListener(e -> { if (listener != null) listener.onFailure(e.getMessage()); });
    }

    public interface OnNotificationListener {
        void onSuccess(String notificationId);
        void onFailure(String error);
    }

    public void getUserNotifications(String userId, int limit, OnNotificationsListener listener) {
        // validate inputs
        if (userId == null || userId.isEmpty()) {

            listener.onFailure("User ID required");
            return;
        }
        db.collection("users")
        // validate inputs
                .document(userId)
                .collection("notifications")
                .whereEqualTo("status", "unread")
                .limit(limit)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Notification> list = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) {
                            n.setId(doc.getId());
                            list.add(n);
                        }
                    }
                    listener.onSuccess(list);
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public interface OnNotificationsListener {
        void onSuccess(List<Notification> notifications);
        void onFailure(String error);
    }
}
