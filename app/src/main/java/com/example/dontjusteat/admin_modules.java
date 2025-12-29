package com.example.dontjusteat;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.content.Intent;

public class admin_modules {
    // Handle navigation for admin pages
    public static void handleMenuNavigation(Activity activity) {

        // Get the current activity name to check which admin page we're on
        String activityName = activity.getClass().getSimpleName();
        // Log.d("ACTIVITY_CHECK", "Current Activity: " + activityName); Used for debugging

        // Get the menu container views for each navigation item
        View home_icon_container = activity.findViewById(R.id.navigate_to_booking);
        View notifications_icon_container = activity.findViewById(R.id.notification);
        View profile_icon_container = activity.findViewById(R.id.navigate_to_customer_profile);
        View my_booking_container = activity.findViewById(R.id.my_bookings_container);

        // Get the button image views
        ImageView menuHomeButton = activity.findViewById(R.id.navigate_to_booking_button);
        ImageView notificationsButton = activity.findViewById(R.id.navigate_to_notification_button);
        ImageView profileButton = activity.findViewById(R.id.navigate_to_customer_profile_button);
        ImageView myBookingButton = activity.findViewById(R.id.navigate_to_my_booking_button);

        // Safety check so it doesn't crash if a layout doesn't have the menu
        if (menuHomeButton == null || notificationsButton == null || profileButton == null || myBookingButton == null) {
            Log.e("ACTIVITY_CHECK_ERROR", "This activity was not found : " + activityName);
            return;
        }

        // Show all buttons as inactive by default
        menuHomeButton.setImageResource(R.drawable.home_inactive);
        notificationsButton.setImageResource(R.drawable.notification_inactive);
        profileButton.setImageResource(R.drawable.profile_inactive);
        myBookingButton.setImageResource(R.drawable.my_booking_inactive);

        // Check which admin page is currently active and show the active icon
        switch (activityName) {
            case "admin_dashboard":
                menuHomeButton.setImageResource(R.drawable.home_active);
                break;

            case "admin_my_notifications":
                notificationsButton.setImageResource(R.drawable.notification_active);
                break;

            case "admin_profile":
                profileButton.setImageResource(R.drawable.profile_active);
                break;

            case "admin_my_bookings":
                myBookingButton.setImageResource(R.drawable.my_booking_active);
                break;
        }

        // Set up navigation for each menu button
        setupMenuClickListener(home_icon_container, menuHomeButton, activityName, activity, admin_dashboard.class);
        setupMenuClickListener(notifications_icon_container, notificationsButton, activityName, activity, null);
        setupMenuClickListener(profile_icon_container, profileButton, activityName, activity, null);
        setupMenuClickListener(my_booking_container, myBookingButton, activityName, activity, null);
    }

    // Helper method to set up click listeners for menu items
    private static void setupMenuClickListener(View container, ImageView button, String currentActivityName, Activity activity, Class<?> targetActivity) {
        // Create a click listener that navigates to the target activity if we're not already there
        View.OnClickListener listener = v -> {
            if (targetActivity != null && !currentActivityName.equals(targetActivity.getSimpleName())) {
                Intent intent = new Intent(activity, targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        };

        // Apply the listener to both the container and the button
        container.setOnClickListener(listener);
        button.setOnClickListener(listener);
    }
}




