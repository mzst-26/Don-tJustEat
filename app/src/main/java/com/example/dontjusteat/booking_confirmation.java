package com.example.dontjusteat;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;


public class booking_confirmation extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_confirmation);
        Modules.applyWindowInsets(this, R.id.rootView);

    }


}
