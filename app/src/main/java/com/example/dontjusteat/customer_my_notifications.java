package com.example.dontjusteat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.dontjusteat.models.Notification;
import com.example.dontjusteat.repositories.NotificationRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class customer_my_notifications extends BaseActivity {
    LinearLayout notificationsContainer;
    TextView noNotificationText;

    private static class NotificationCard {
        String ID;
        String title;
        String message;
        String date;
        String time;
        String status;
        String bookingId;
        String restaurantId;


        NotificationCard(String ID, String titleText, String messageText, String dateText, String timeText, String statusText, String bookingId, String restaurantId) {
            this.ID = ID;
            this.message = messageText;
            this.title = titleText;
            this.date = dateText;
            this.time = timeText;
            this.status = statusText;
            this.bookingId = bookingId;
            this.restaurantId = restaurantId;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireCustomerOrFinish()) {
            return;
        }
        setContentView(R.layout.customer_my_notifications);
        TextView title = findViewById(R.id.location_name);
        title.setText("Notifications & Updates");

        //import modules
        Modules.applyWindowInsets(this, R.id.rootView);
        Modules.handleMenuNavigation(this);
        Modules.handleSimpleHeaderNavigation(this);

        // Initialize the container and no notification text
        notificationsContainer = findViewById(R.id.content_container);
        noNotificationText = findViewById(R.id.no_notification_text);

        loadNotifications();

    }

    private void loadNotifications() {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid == null) {
            handleNoNotification(true);
            return;
        }

        NotificationRepository repo = new NotificationRepository();
        // load notifications
        repo.getUserNotifications(uid, 50, new NotificationRepository.OnNotificationsListener() {
            @Override
            // render notifications
            public void onSuccess(List<Notification> notifications) {
                List<NotificationCard> cards = new ArrayList<>();
                // convert notifications to cards
                SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
                SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.UK);


                // add cards to list
                for (Notification n : notifications) {
                    String id = n.getId() != null ? n.getId() : "";
                    String title = n.getTitle() != null ? n.getTitle() : "Notification";
                    String msg = n.getDescription() != null ? n.getDescription() : "";
                    String status = n.getStatus() != null ? n.getStatus() : "unread";
                    String date = n.getCreatedAt() != null ? dateFmt.format(n.getCreatedAt().toDate()) : "";
                    String time = n.getCreatedAt() != null ? timeFmt.format(n.getCreatedAt().toDate()) : "";
                    String bookingId = n.getBookingId();
                    String restaurantId = n.getRestaurantId();

                    cards.add(new NotificationCard(id, title, msg, date, time, status, bookingId, restaurantId));
                }
                // render cards
                if (cards.isEmpty()) {
                    handleNoNotification(true);
                } else {
                    handleNoNotification(false);
                    renderNotifications(cards);
                }

            }

            @Override
            public void onFailure(String error) {
                handleNoNotification(true);
            }
        });
    }

    private void renderNotifications(List<NotificationCard> cards) {
        // inflate the card layout
        LayoutInflater inflater = LayoutInflater.from(this);

        // clear old notification cards but keep the no_notification_text
        // Remove the no notification text temporarily
        if (noNotificationText != null && noNotificationText.getParent() != null) {
            notificationsContainer.removeView(noNotificationText);
        }

        // clear all other views
        notificationsContainer.removeAllViews();

        // Add back the no notification text at the top
        if (noNotificationText != null) {
            notificationsContainer.addView(noNotificationText, 0);
        }

        for (NotificationCard cardData : cards) {
            View Cardview = inflater.inflate(R.layout.component_notification_card, notificationsContainer, false);

            //get the references
            TextView notification_title = Cardview.findViewById(R.id.notification_title);
            TextView notification_message = Cardview.findViewById(R.id.notification_message);
            TextView notification_date = Cardview.findViewById(R.id.notification_date);
            TextView notification_time = Cardview.findViewById(R.id.notification_time);
            ImageButton markAsRead = Cardview.findViewById(R.id.notification_mark_read);

            //set the data to the views
            notification_title.setText(cardData.title);
            notification_message.setText(cardData.message);
            notification_date.setText(cardData.date);
            notification_time.setText(cardData.time);
            // per-card click handler for mark-as-read
            markAsRead.setOnClickListener(v -> {
                String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                NotificationRepository repo = new NotificationRepository();
                if (uid != null) {
                    repo.updateNotificationStatus(uid, cardData.ID, "read", new NotificationRepository.OnNotificationListener() {
                        @Override
                        public void onSuccess(String notificationId) {
                            cards.removeIf(c -> c.ID.equals(notificationId));
                            renderNotifications(cards);
                            handleNoNotification(cards.isEmpty());
                        }

                        @Override
                        public void onFailure(String error) {
                            cards.removeIf(c -> c.ID.equals(cardData.ID));
                            renderNotifications(cards);
                            handleNoNotification(cards.isEmpty());
                        }
                    });
                } else {
                    cards.removeIf(c -> c.ID.equals(cardData.ID));
                    renderNotifications(cards);
                    handleNoNotification(cards.isEmpty());
                }
            });


            // add this card to the container to be displayed
            notificationsContainer.addView(Cardview);

        }


    }

    private void handleNoNotification(Boolean noNotification) {
        if (noNotificationText != null) {
            noNotificationText.setVisibility(noNotification ? TextView.VISIBLE : TextView.GONE);
        }
    }


}
