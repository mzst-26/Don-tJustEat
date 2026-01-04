package com.example.dontjusteat;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

<<<<<<< HEAD
import com.example.dontjusteat.security.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.firestore.DocumentSnapshot;

=======
>>>>>>> origin/main
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

        View home_icon_container = activity.findViewById(R.id.navigate_to_booking);
        View notifications_icon_container = activity.findViewById(R.id.notification);
        View profile_icon_container = activity.findViewById(R.id.navigate_to_customer_profile);
        View my_booking_container = activity.findViewById(R.id.my_bookings_container);

        //get the buttons themselves
        ImageView menuHomeButton = activity.findViewById(R.id.navigate_to_booking_button);
        ImageView notificationsButton = activity.findViewById(R.id.navigate_to_notification_button);
        ImageView profileButton = activity.findViewById(R.id.navigate_to_customer_profile_button);
        ImageView myBookingButton = activity.findViewById(R.id.navigate_to_my_booking_button);

        //Safety check so it doesn't crash if a layout doesn't have the menu
        if (menuHomeButton == null || notificationsButton == null || profileButton == null || myBookingButton == null) {
            Log.e("ACTIVITY_CHECK_ERROR", "This activity was not found : " + activityName);
            return;
        }

        // show all by default
        menuHomeButton.setImageResource(R.drawable.home_inactive);
        notificationsButton.setImageResource(R.drawable.notification_inactive);
        profileButton.setImageResource(R.drawable.profile_inactive);
        myBookingButton.setImageResource(R.drawable.my_booking_inactive);


        // hide the icon for the current screen by hiding the container of the button
        switch (activityName) {
            case "customer_booking":
                menuHomeButton.setImageResource(R.drawable.home_active);
                break;

            case "customer_my_notifications":
                notificationsButton.setImageResource(R.drawable.notification_active);
                break;

            case "customer_profile":
                profileButton.setImageResource(R.drawable.profile_active);
                break;
            case "my_bookings":
                myBookingButton.setImageResource(R.drawable.my_booking_active);
                break;
        }

        // Set the navigation of the buttons
        setMenuItemClick(home_icon_container, menuHomeButton, activityName, activity, customer_booking.class);
        setMenuItemClick(notifications_icon_container, notificationsButton, activityName, activity, customer_my_notifications.class);
        setMenuItemClick(profile_icon_container, profileButton, activityName, activity, customer_profile.class);
        setMenuItemClick(my_booking_container, myBookingButton, activityName, activity, my_bookings.class);

    }
    private static void setMenuItemClick(
            View container,
            ImageView button,
            String activityName,
            Activity activity,
            Class<?> targetActivity
    ) {
        View.OnClickListener listener = v -> {
            if (!activityName.equals(targetActivity.getSimpleName())) {
                Intent intent = new Intent(activity, targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
            }
        };

        container.setOnClickListener(listener);
        button.setOnClickListener(listener);
    }

    public static void handleSimpleHeaderNavigation(Activity activity){
        ImageView backButton = activity.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> activity.finish());
    }


<<<<<<< HEAD
    public static void logoutAction(Activity activity){
        // Clear local session
        try {
            new SessionManager(activity).clearSession();
        } catch (Exception ignored) { }

        // Sign out from Firebase
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception ignored) { }

        // sign out Google if it was used
        try {
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_SIGN_IN);
            // clear google cache
            googleSignInClient.signOut();
            //fully revoke access
            googleSignInClient.revokeAccess();
        } catch (Exception ignored) { }

        // navigate to main activity
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }




=======
>>>>>>> origin/main

}
