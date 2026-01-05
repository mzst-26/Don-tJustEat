package com.example.dontjusteat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class booking_confirmation extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (!requireCustomerOrFinish()) {
            return;
        }
        setContentView(R.layout.booking_confirmation);
        Modules.applyWindowInsets(this, R.id.rootView);

        // Handle menu navigation
        Modules.handleMenuNavigation(this);

        //handle the notification button
        handleNotificationNavigationButton();
        //handle the my booking button
        handleViewBookingNavigationButton();

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
