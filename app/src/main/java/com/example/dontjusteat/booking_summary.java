package com.example.dontjusteat;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;


public class booking_summary extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_summary);

        //make back button responsive
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // define the options for the slider
        final String[] timeOptions = {"18:00", "18:30", "19:00"};

        //bind views
        TextView tvDiningTime = findViewById(R.id.tv_dining_time);
        Slider sliderDiningTime = findViewById(R.id.slider_dining_time);

        //set initial value
        //the slider starts at 0.0 by default, so we set the text to the first option
        tvDiningTime.setText(timeOptions[0]);

        // Lable formater to show time instead of numbers
        sliderDiningTime.setLabelFormatter(new LabelFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < timeOptions.length) {
                    return timeOptions[index];
                }
                return "";
            }
        });

        //add listener to update text when slider value changes
        sliderDiningTime.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                //cast the float value to the int index
                int index = (int) value;

                //Safety check
                if (index >= 0 && index < timeOptions.length) {
                    tvDiningTime.setText(timeOptions[index]);
                }
            }
        });

    }
}
