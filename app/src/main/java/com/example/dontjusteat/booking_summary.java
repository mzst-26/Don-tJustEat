package com.example.dontjusteat;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

public class booking_summary extends AppCompatActivity {

    // --- Data Structures ---
    private static class TableOptions {
        String name;
        int size;
        String[] availableTimes;



        TableOptions(String name, int size, String[] availableTimes) {
            this.name = name;
            this.size = size;
            this.availableTimes = availableTimes;
        }
    }


    //define user data types and structures
    private static class UserInfo {
        String name;
        String phoneNumber;
        String email;

        // Constructor
        UserInfo(String name, String phoneNumber, String email) {
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.email = email;
        }
    }


    // --- states & Data ---
    private TableOptions[] availableTables;
    private String[] currentTimeOptions; // mutable list of times based on selected table

    // --- Ui Components ---
    private TextView tvDiningTime;
    private Slider sliderDiningTime;
    private TextView tvTableName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_summary);

        // get initial mock data
        initData();

        // Orchestrator: Initialize separate functional modules
        initializeNavigationService();
        initializeDiningTimeService(); // Must be init first to setup views
        initializeTableService();      // Depends on Dining Time Service
    }

    //I will later replace this with the database data
    private void initData() {
        // different tables have different available times
        availableTables = new TableOptions[]{
                new TableOptions("Table 1 (4 Seats)", 4, new String[]{"18:00", "19:00", "20:00"}),
                new TableOptions("Table 2 (2 Seats)", 2, new String[]{"17:30", "18:30", "21:00"}),
                new TableOptions("Table 3 (6 Seats)", 6, new String[]{"19:00", "20:00"}),
                new TableOptions("Table 4 (4 Seats)", 4, new String[]{"20:00", "20:30", "21:00", "21:30"})
        };
        // Default to first table's times
        currentTimeOptions = availableTables[0].availableTimes;

        UserInfo userInfo = new UserInfo(
                "John Doe",
                "1234567890", "jhondoe@example.com"
        );

        fillUserData(userInfo);
    }

    //I have put the code in sections to make it clean and easy to navigate when trying to edit.
    // I will later do the same with the rest of the code
    /**
     * Section 1: navigation
     */
    private void initializeNavigationService() {
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Section 2: Dining Time Service
     * this will handle time selection logic.
     */
    private void initializeDiningTimeService() {
        //get elements from layout
        tvDiningTime = findViewById(R.id.tv_dining_time);
        sliderDiningTime = findViewById(R.id.slider_dining_time);

        // first configuration
        updateDiningTimeUI();

        // formatter
        sliderDiningTime.setLabelFormatter(value -> {
            // converting the slider value to index
            int index = (int) value;

            if (isTimeOptionValidIndex(index)) {
                return currentTimeOptions[index];
            }
            return "";
        });

        // listener
        // slider value changes to show the selected time for booking
        sliderDiningTime.addOnChangeListener((slider, value, fromUser) -> {
            int index = (int) value;
            if (isTimeOptionValidIndex(index)) {
                // update the text view with the selected time
                tvDiningTime.setText(currentTimeOptions[index]);
            }
        });
    }

    /**
     * Section 3: table service
     *  to handle table selection logic.
     */
    private void initializeTableService() {
        //get elements from layout
        tvTableName = findViewById(R.id.tv_table_name);
        Slider sliderTable = findViewById(R.id.slider_table_selection);

        //set slider based on number of tables available
        sliderTable.setValueFrom(0);
        sliderTable.setValueTo(availableTables.length - 1); // this would be the index of the last table
        sliderTable.setStepSize(1);
        sliderTable.setValue(0);

        // default initial text
        tvTableName.setText(availableTables[0].name);

        // formatter
        // slider value changes to show the selected table for booking
        sliderTable.setLabelFormatter(value -> {
            int index = (int) value;
            if (index >= 0 && index < availableTables.length) {
                //update the text view with the selected table
                return availableTables[index].name;
            }
            return "";
        });

        // Listener then update times when table changes
        sliderTable.addOnChangeListener((slider, value, fromUser) -> {
            int index = (int) value;
            // update the text view with the selected table
            if (index >= 0 && index < availableTables.length) {
                TableOptions selectedTable = availableTables[index];
                tvTableName.setText(selectedTable.name);
                
                // this is the helper logic that fetchs times for this table and update the other slider
                updateDiningTimes(selectedTable.availableTimes);
            }
        });
    }



    //called when a table is selected to refresh the time slider
    private void updateDiningTimes(String[] newTimes) {
        // update the times
        this.currentTimeOptions = newTimes;

        // Reset slider value first to avoid out-of-bounds
        sliderDiningTime.setValue(0);
        
        // Update range, this will set the range to the length of the new times
        sliderDiningTime.setValueTo(Math.max(0, newTimes.length - 1));
        
        // update UI text
        updateDiningTimeUI();
    }

    // update the text view with the selected time
    private void updateDiningTimeUI() {
        if (currentTimeOptions.length > 0) {
            tvDiningTime.setText(currentTimeOptions[0]);
        } else {
            tvDiningTime.setText("N/A");
        }
    }

    // helper method to check if the index is valid to avoid out-of-bound
    private boolean isTimeOptionValidIndex(int index) {
        return index >= 0 && index < currentTimeOptions.length;
    }

    //fill in user data
    private void fillUserData(UserInfo userInfo) {

        //get layout elements
        TextView tvName = findViewById(R.id.name);
        TextView tvPhoneNumber = findViewById(R.id.phone_number);
        TextView tvEmail = findViewById(R.id.email);

        //set text
        tvName.setText(userInfo.name);
        tvPhoneNumber.setText(userInfo.phoneNumber);
        tvEmail.setText(userInfo.email);

    }
}
