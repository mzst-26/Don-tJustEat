package com.example.dontjusteat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dontjusteat.models.User;
import com.example.dontjusteat.security.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

// the base activity handles common defensive checks across all activities
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // later might add hooks, themes here.
    }

    // check if the user is authenticated
    @Override
    protected void onResume() {
        super.onResume();
        enforceAuthenticatedUser();
    }


    // check if the user is authenticated
    protected void enforceAuthenticatedUser() {
        // make sure that firebase user and local sessions exist
        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        boolean isEmailVerified = FirebaseAuth.getInstance().getCurrentUser().isEmailVerified();
        SessionManager sm = new SessionManager(this);
        SessionManager.SessionData session = sm.getSession();

        if (!isLoggedIn || session == null || !isEmailVerified) {
            // Clear any partial state and route to login
            sm.clearSession();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class); // or a dedicated Login activity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }


   //check if the user is an admin and is logged in
    protected boolean requireAdminOrFinish() {
        SessionManager sm = new SessionManager(this);
        SessionManager.SessionData session = sm.getSession();
        if (session == null || !session.isStaff) {
            // Not an admin: route to main customer flow
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    //check if the user is a customer and is logged in
    protected boolean requireCustomerOrFinish() {
        SessionManager sm = new SessionManager(this);
        SessionManager.SessionData session = sm.getSession();
        if (session == null || !session.isCustomer) {
            // If staff reached a customer-only page, route them to admin home
            if (session != null && session.isStaff) {
                Intent adminIntent = new Intent(this, admin_dashboard.class);
                adminIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(adminIntent);
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finish();
            return false;
        }
        return true;
    }
}
