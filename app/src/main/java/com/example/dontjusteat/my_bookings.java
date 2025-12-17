package com.example.dontjusteat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class my_bookings extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_bookings);

        Modules.applyWindowInsets(this, R.id.rootView);
        Modules.handleMenuNavigation(this);



    }
}
