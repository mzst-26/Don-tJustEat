package com.example.dontjusteat;

import android.os.Bundle;

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
        List<NotificationCard> data;

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


        cards.add(new customer_my_notifications.NotificationCard(
                "12345",
                "New Booking",
                "You have a new booking!",
                "08/05/2025",
                "19:30",
                "Pending"
                ));



        //pull all notifications
        pullNotifications();

        //render all notifications
        renderNotifications();

        //handle notification click
    }


    private void pullNotifications() {


    }

    private void renderNotifications() {

    }

    private void handleNotificationClick() {

    }


}
