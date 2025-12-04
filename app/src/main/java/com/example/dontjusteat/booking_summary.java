package com.example.dontjusteat;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

public class booking_summary extends AppCompatActivity {
    private final String[] timeOptions = {"18:00", "18:30", "19:00"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_summary);

        // Orchestrator: Initialize separate functional modules
        initializeNavigationService();
        initializeDiningTimeService();
    }

    /**
     * Section 1: naivatoin or routing Service
     * Handles header interactions and navigation logic.
     */
    private void initializeNavigationService() {
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Section 2: dining Time Service
     * This handles the business logic for time selection, UI updates, and slider configuration.
     */
    private void initializeDiningTimeService() {
        // Bind Views
        TextView tvDiningTime = findViewById(R.id.tv_dining_time);
        Slider sliderDiningTime = findViewById(R.id.slider_dining_time);

        // 1. Set Initial State
        tvDiningTime.setText(timeOptions[0]);

        // 2. Configure Formatter (UI Presentation)
        sliderDiningTime.setLabelFormatter(new LabelFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (isTimeOptionValidIndex(index)) {
                    return timeOptions[index];
                }
                return "";
            }
        });

        // 3. Configure Listener (Interaction Logic)
        sliderDiningTime.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                int index = (int) value;
                if (isTimeOptionValidIndex(index)) {
                    tvDiningTime.setText(timeOptions[index]);
                }
            }
        });
    }

    // Helper method to ensure data integrity
    private boolean isTimeOptionValidIndex(int index) {
        return index >= 0 && index < timeOptions.length;
    }


}
