package com.example.dontjusteat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class admin_dashboard extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);
        Modules.applyWindowInsets(this, R.id.rootView);
    }
}
