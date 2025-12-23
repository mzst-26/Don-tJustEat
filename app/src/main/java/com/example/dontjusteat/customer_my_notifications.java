package com.example.dontjusteat;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class customer_my_notifications extends AppCompatActivity {

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
        setContentView(R.layout.customer_my_notifications);

        //import modules
        Modules.applyWindowInsets(this, R.id.rootView);
        Modules.handleMenuNavigation(this);
        Modules.handleSimpleHeaderNavigation(this);

        // Fake data for now than later I will replace it with the real data from the DB
        List<customer_my_notifications.NotificationCard> cards = new ArrayList<>();

        // Pass the list to pullNotifications
        pullNotifications(cards);

        if(cards.isEmpty()){
            handleNoNotification (true);
        }else {
            handleNoNotification(false);
            renderNotifications();
        }
    }


    private void pullNotifications(List<customer_my_notifications.NotificationCard> cards) {

        // add a sample notification card
        cards.add(new customer_my_notifications.NotificationCard(
                "12345",
                "New Booking",
                "You have a new booking!",
                "08/05/2025",
                "19:30",
                "Pending"
        ));


    }

    private void renderNotifications() {

    }

    private void handleNotificationClick() {

    }

    private void handleNoNotification(Boolean noNotification) {
        if (noNotification) {
            TextView noNotificationText = findViewById(R.id.no_notification_text);
            noNotificationText.setVisibility(TextView.VISIBLE);
        } else {
            TextView noNotificationText = findViewById(R.id.no_notification_text);
            noNotificationText.setVisibility(TextView.GONE);
        }

    }


}
