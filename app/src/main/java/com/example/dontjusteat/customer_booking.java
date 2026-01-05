package com.example.dontjusteat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.Calendar;

public class customer_booking extends BaseActivity {

    private LinearLayout bottomSheet;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    private View header;
    private View mapView;
    private View handle;
    private ImageView editButton;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;

    private TextView txtCity, txtDate, txtTime, txtGuests;

    private final String[] cityList = new String[]{
            "London", "Bristol", "Manchester", "Liverpool", "Leeds",
            "Edinburgh", "Cardiff", "Birmingham", "Glasgow", "Newcastle"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireCustomerOrFinish()) {
            return;
        }
        setContentView(R.layout.customer_booking);

        initViews(); // initializing the views
        setupBottomSheetBehavior(); // setting up the bottom sheet
        setupHandleTouchControl(); // setting up the handle for the bottom sheet
        calculatePeekHeightAndOffsets(); // calculating the peek height and offsets for the bottom sheet
        setupButtonClick(); // setting up the select button for booking
        setupEditButton(); // setting up the edit button for search function
        applySystemUIHandling(); // setting up the system UI handling


    }


    // INITIALIZATION
    private void initViews() {
        //for bottom sheet:
        bottomSheet = findViewById(R.id.list);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        header = findViewById(R.id.header_container);
        handle = findViewById(R.id.handle_container);
        mapView = findViewById(R.id.mapView);

        //for search filter button:
        editButton = findViewById(R.id.edit_icon);
        txtCity = findViewById(R.id.searched_city);
        txtDate = findViewById(R.id.searched_date);
        txtTime = findViewById(R.id.searched_time);
        txtGuests = findViewById(R.id.searched_number_of_guests);

    }


    // BOTTOM SHEET SETUP
    private void setupBottomSheetBehavior() {
        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setDraggable(false);
    }

    private void setupHandleTouchControl() {
        handle.setOnTouchListener((v, event) -> {
            //handle touch events for the handle
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // start dragging the bottom sheet when the handle is pressed
                    bottomSheetBehavior.setDraggable(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    bottomSheetBehavior.setDraggable(false);
                    break;
            }
            return false;
        });
    }

    // SEARCH FILTER BUTTON SET UP
    private void setupEditButton() {
        if (editButton != null) {
            editButton.setOnClickListener(v -> showSearchFilterDialog());
        }
    }



    // LAYOUT
    private void calculatePeekHeightAndOffsets() {
        bottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        bottomSheet.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        //calculate the peek height and offsets based on the view hierarchy
                        int parentHeight = ((View) bottomSheet.getParent()).getHeight();
                        int mapBottom = mapView.getBottom();
                        int peekHeight = parentHeight - mapBottom;

                        // set the peek height and offsets for the bottom sheet
                        bottomSheetBehavior.setPeekHeight(peekHeight + dpToPx(100));
                        bottomSheetBehavior.setExpandedOffset(header.getHeight());
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
        );
    }


    private void showSearchFilterDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search_filter, null);

        AutoCompleteTextView inputCity = dialogView.findViewById(R.id.input_city);
        EditText inputDate = dialogView.findViewById(R.id.input_date);
        EditText inputGuests = dialogView.findViewById(R.id.input_guests);

        builder.setView(dialogView);
        // CITY AUTOCOMPLETE
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                cityList
        );
        inputCity.setAdapter(adapter);
        inputCity.setThreshold(1);

        //set up date and time picker
        inputDate.setOnClickListener(v -> pickDateAndTime(inputDate));
        //the positive button for the pop up dialog to submit a search
        builder.setPositiveButton("Search", (dialog, which) -> {
            //check if the input is valid
            String city = safeText(inputCity);
            // give a toast if the city is invalid
            if (!isValidCity(city)) {
                Toast.makeText(this, "Please select a valid city from the list", Toast.LENGTH_SHORT).show();
                return; // do not close or proceed
            }
            // update the search UI with the input values if the value is valid
            updateSearchUI(
                    safeText(inputCity),
                    safeText(inputDate),
                    safeText(inputGuests)
            );
        });

        // create a cancel dialog button and close when clicked on
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        //check if the dialog has a valid window and set the background to transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        // show the dialog
        dialog.show();
    }


    private void pickDateAndTime(EditText inputDate) {
        // get the current date
        final Calendar c = Calendar.getInstance();
        // create a date picker dialog and set the current date as the default
        datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    // after picking date then pick time
                    timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hour, minute) -> {

                                String date = dayOfMonth + "/" + (month + 1); // we only need the day and month
                                // format the time to hh:mm
                                String time = String.format("%02d:%02d", hour, minute);

                                inputDate.setText(date + " after " + time);
                            },
                            c.get(Calendar.HOUR_OF_DAY),
                            c.get(Calendar.MINUTE),
                            true
                    );
                    // show the time picker dialog
                    timePickerDialog.show();

                },
                // set the current date as the default
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        // show the date picker dialog
        datePickerDialog.show();
    }


    // BUTTON ACTIONS
    private void setupButtonClick() {
        Button selectButton = findViewById(R.id.select_button);
        selectButton.setOnClickListener(v -> openLocationDetails());
    }

    private void openLocationDetails() {
        Intent intent = new Intent(this, customer_location_detail.class);
        startActivity(intent);
    }


    // SYSTEM UI OR INSETS
    private void applySystemUIHandling() {
        Modules.applyWindowInsets(this, R.id.rootView);
        Modules.handleMenuNavigation(this);
    }

    private void updateSearchUI(String city, String dateTime, String guests) {
        // update the search UI with the input values
        //check if the city is not null
        if (txtCity != null) { txtCity.setText(city);} else{ txtCity.setText(cityList[0]);}
        //check if the date and time are not null
        if (txtDate != null || txtTime != null) {
            String[] parts = dateTime.split(" after ");
            // check if they are seperated by a space correctly or not
            if (parts.length == 2) {
                // Display the content
                txtDate.setText(parts[0]);
                txtTime.setText("After " + parts[1]);
            }
        }

        if (txtGuests != null) txtGuests.setText("Table for " + guests);

        Toast.makeText(this, "Searching in: " + city, Toast.LENGTH_SHORT).show();
    }




    // UTILS
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }


    private String safeText(EditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }

    private String safeText(AutoCompleteTextView et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }

    private boolean isValidCity(String city) {
        for (String c : cityList) {
            if (c.equalsIgnoreCase(city.trim())) {
                return true;
            }
        }
        return false;
    }

}
