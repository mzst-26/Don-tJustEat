package com.example.dontjusteat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
