package com.example.dontjusteat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class customer_login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This line loads your XML layout and displays it on the screen
        setContentView(R.layout.customer_login);

        //Back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        //Sign in with email button (for testing only)
        Button signInWithEmailButton = findViewById(R.id.sign_in_with_email);

        signInWithEmailButton.setOnClickListener(v -> {
            Intent intent = new Intent(customer_login.this, customer_booking.class);
            startActivity(intent);
        });

    }
}
