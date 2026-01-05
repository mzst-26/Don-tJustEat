package com.example.dontjusteat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.dontjusteat.security.SessionManager;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Check session first (faster than Firebase check)
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.getSession().isStaff) {
                startActivity(new Intent(this, admin_dashboard.class));
                finish();
                return;
            } else if (sessionManager.getSession().isCustomer) {
                // already logged in as a customer; send to customer home
                startActivity(new Intent(this, customer_booking.class));
                finish();
                return;
            }
        }


        // Customer access button
        Button customerButton = findViewById(R.id.button_Customer_Access);
        customerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, customer_login.class);
            startActivity(intent);
        });

        //Staff access button
        Button StaffAccessButton = findViewById(R.id.button_Staff_Access);
        StaffAccessButton.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, staff_login.class);
            startActivity(intent);
        });
    }
}
