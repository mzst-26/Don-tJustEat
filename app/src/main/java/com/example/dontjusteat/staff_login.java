package com.example.dontjusteat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class staff_login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This line loads your XML layout and displays it on the screen
        setContentView(R.layout.staff_login);

        //Back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        //Sign in with email button just for testing while building ui
        Button signInWithEmailButton = findViewById(R.id.admin_sign_in_with_email);
        
        signInWithEmailButton.setOnClickListener(v -> {
            //navigate to admin dashboard for testing
            Intent intent = new Intent(staff_login.this, admin_dashboard.class);
            startActivity(intent);
        });
    }
}
