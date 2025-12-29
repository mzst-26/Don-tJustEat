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


public class admin_notifications extends AppCompatActivity {
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
        setContentView(R.layout.admin_notifications);

        //import modules
        Modules.applyWindowInsets(this, R.id.rootView);
        admin_modules.handleMenuNavigation(this);
        Modules.handleSimpleHeaderNavigation(this);

        // fake data for now than later I will replace it with the real data from the DB
        List<admin_notifications.NotificationCard> cards = new ArrayList<>();

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


    private void pullNotifications(List<admin_notifications.NotificationCard> cards) {

        // New booking request from customer
        cards.add(new admin_notifications.NotificationCard(
                "12345",
                "New Booking Request",
                "Alice Johnson requested a booking for 2 guests at Table 3",
                "12/29/2025",
                "12:15 PM",
                "Pending"
        ));

        // customer requested a change to their booking
        cards.add(new admin_notifications.NotificationCard(
                "12346",
                "Booking Change Request",
                "Brian Lee requested to change their booking time from 12:45 PM to 1:00 PM",
                "12/29/2025",
                "12:45 PM",
                "Pending"
        ));

        // customer cancelled their booking
        cards.add(new admin_notifications.NotificationCard(
                "12347",
                "Cancellation Request",
                "Carmen Diaz requested to cancel their booking for 3 guests",
                "12/29/2025",
                "1:00 PM",
                "Pending"
        ));

        //  booking confirmed (action completed)
        cards.add(new admin_notifications.NotificationCard(
                "12348",
                "Booking Confirmed",
                "David Chen's booking for 6 guests at Table 4 has been confirmed",
                "12/29/2025",
                "1:30 PM",
                "Completed"
        ));

        // customer change request approved
        cards.add(new admin_notifications.NotificationCard(
                "12349",
                "Change Approved",
                "Eva Müller's table change has been approved - Table 5 at 2:00 PM",
                "12/29/2025",
                "2:00 PM",
                "Completed"
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
