package com.example.dontjusteat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class admin_manage_menu extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_menu);

        //import modules
        Modules.applyWindowInsets(this, R.id.rootView);
        admin_modules.handleMenuNavigation(this);
        Modules.handleSimpleHeaderNavigation(this);

    }
}
