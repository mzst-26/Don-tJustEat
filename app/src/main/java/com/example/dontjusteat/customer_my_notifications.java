package com.example.dontjusteat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


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


        NotificationCard(String ID, String titleText, String messageText, String dateText, String timeText, String statusText) {
            this.ID = ID;
            this.message = messageText;
            this.title = titleText;
            this.date = dateText;
            this.time = timeText;
            this.status = statusText;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireCustomerOrFinish()) {
            return;
        }
        setContentView(R.layout.customer_my_notifications);

        //import modules
        Modules.applyWindowInsets(this, R.id.rootView);
        Modules.handleMenuNavigation(this);
        Modules.handleSimpleHeaderNavigation(this);

        // Fake data for now than later I will replace it with the real data from the DB
        List<customer_my_notifications.NotificationCard> cards = new ArrayList<>();

        // Pass the list to pullNotifications
        pullNotifications(cards);

        // Initialize the container and no notification text
        notificationsContainer = findViewById(R.id.content_container);
        noNotificationText = findViewById(R.id.no_notification_text);

        if(cards.isEmpty()){
            handleNoNotification (true);
        }else {
            handleNoNotification(false);
            renderNotifications(cards);
        }
    }


    private void pullNotifications(List<customer_my_notifications.NotificationCard> cards) {

        // add a sample notification card
        cards.add(new customer_my_notifications.NotificationCard(
                "12345",
                "New Booking",
                "We have received your booking request!",
                "08/05/2025",
                "19:30",
                "Pending"
        ));

        // add a sample notification card
        cards.add(new customer_my_notifications.NotificationCard(
                "12346",
                "Booking Confirmed",
                "Your booking request has been confirmed!",
                "08/05/2025",
                "19:45",
                "pending"
        ));

        // add a sample notification card
        cards.add(new customer_my_notifications.NotificationCard(
                "12347",
                "Change To Booking",
                "Your change has been submitted!",
                "08/05/2025",
                "19:50",
                "pending"
        ));




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
                //find the card in the list and remove the cards
                for (int i = 0; i < cards.size(); i++) {
                    if (cards.get(i).ID.equals(cardData.ID)) {
                        cards.remove(i);
                        break;
                    }
                }
                renderNotifications(cards);
                handleNoNotification(cards.isEmpty());

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
