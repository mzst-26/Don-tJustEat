package com.example.dontjusteat;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class Modules {

    // Call this AFTER setContentView(...), pass your actual root view id
    public static void applyWindowInsets(Activity activity, int rootViewId) {

        Window window = activity.getWindow();

        // this allows manual control of insets
        WindowCompat.setDecorFitsSystemWindows(window, false);

        View root = activity.findViewById(rootViewId);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;

                // prevent page from going behind system bars
                mlp.topMargin = bars.top;      // status bar height
                mlp.bottomMargin = bars.bottom; // nav bar height

                v.setLayoutParams(mlp);
            }

            return insets;
        });

        ViewCompat.requestApplyInsets(root);
    }

    public static void handleMenuNavigation(Activity activity) {

        String activityName = activity.getClass().getSimpleName();
    //        Log.d("ACTIVITY_CHECK", "Current Activity: " + activityName); I use this for debugging

        //get all containers for the menu icon buttons
        View menuHome = activity.findViewById(R.id.navigate_to_booking);
        View notifications = activity.findViewById(R.id.notification);
        View profile = activity.findViewById(R.id.navigate_to_customer_profile);

        //get the buttons themselves
        View menuHomeButton = activity.findViewById(R.id.navigate_to_booking_button);
        View notificationsButton = activity.findViewById(R.id.navigate_to_notification_button);
        View profileButton = activity.findViewById(R.id.navigate_to_customer_profile_button);

        //Safety check so it doesn't crash if a layout doesn't have the menu
        if (menuHome == null || notifications == null || profile == null) {
            Log.e("ACTIVITY_CHECK_ERROR", "This activity was not found : " + activityName);
            return;
        }

        // show all by default
        menuHome.setVisibility(View.VISIBLE);
        notifications.setVisibility(View.VISIBLE);
        profile.setVisibility(View.VISIBLE);

        // hide the icon for the current screen
        switch (activityName) {
            case "customer_booking":
                menuHome.setVisibility(View.GONE);
                break;

            case "customer_location_detail":
                notifications.setVisibility(View.GONE);
                break;

        }

        // Set the navigation of the buttons

        menuHomeButton.setOnClickListener(v -> {
            if (!activityName.equals("customer_booking")) {
                //create an Intent to start the new activity, this is used to navigate
                Intent intent = new Intent(activity, customer_booking.class);
                activity.startActivity(intent);
            }
        });

        notificationsButton.setOnClickListener(v -> {
            if (!activityName.equals("customer_location_detail")) {
                //create an Intent to start the new activity, this is used to navigate
                Intent intent = new Intent(activity, customer_location_detail.class);
                activity.startActivity(intent);
            }
        });

        profileButton.setOnClickListener(v -> {
            if (!activityName.equals("customer_profile")) {
                //create an Intent to start the new activity, this is used to navigate
                Intent intent = new Intent(activity, customer_location_detail.class);
                activity.startActivity(intent);
            }
        });
    }



}
