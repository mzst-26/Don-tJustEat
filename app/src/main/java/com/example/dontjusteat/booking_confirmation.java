package com.example.dontjusteat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class booking_confirmation extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (!requireCustomerOrFinish()) {
            return;
        }
        setContentView(R.layout.booking_confirmation);
        Modules.applyWindowInsets(this, R.id.rootView);

        // get booking IDs from intent
        ArrayList<String> bookingIds = getIntent().getStringArrayListExtra("bookingIds");

        // display booking reference
        displayBookingReference(bookingIds);

        // Handle menu navigation
        Modules.handleMenuNavigation(this);

        //handle the notification button
        handleNotificationNavigationButton();
        //handle the my booking button
        handleViewBookingNavigationButton();
    }

    // display booking reference numbers
    private void displayBookingReference(ArrayList<String> bookingIds) {
        TextView bookingRefView = findViewById(R.id.booking_reference);

        if (bookingRefView != null && bookingIds != null && !bookingIds.isEmpty()) {
            // format booking IDs for display
            if (bookingIds.size() == 1) {
                // single booking
                bookingRefView.setText("#" + bookingIds.get(0).substring(0, Math.min(8, bookingIds.get(0).length())));
            } else {
                // multiple bookings - show first one with count
                String firstId = bookingIds.get(0).substring(0, Math.min(8, bookingIds.get(0).length()));
                bookingRefView.setText("#" + firstId + " (+" + (bookingIds.size() - 1) + " more)");
            }
        }
    }


    //handle the notification button
    private void handleNotificationNavigationButton(){
        Button notificationButton = findViewById(R.id.navigate_to_notification);
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, customer_my_notifications.class);
            startActivity(intent);
        });
    }

    //handle the my booking button
    private void handleViewBookingNavigationButton(){
        Button myBookingButton = findViewById(R.id.navigate_to_bookings);
        myBookingButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, my_bookings.class);
            startActivity(intent);
        });
    }


}
