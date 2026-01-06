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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


import com.example.dontjusteat.repositories.RestaurantRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.lifecycle.ViewModelProvider;
import com.example.dontjusteat.viewmodel.CustomerBookingViewModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;




import java.util.Calendar;

public class customer_booking extends BaseActivity {
    private CustomerBookingViewModel viewModel;

    private static final int REQ_LOCATION = 1001;
    private FusedLocationProviderClient locationClient;

    private LinearLayout bottomSheet;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;

    private View header;
    private View mapView;
    private View handle;
    private ImageView editButton;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;

    private TextView txtCity, txtDate, txtTime, txtGuests;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireCustomerOrFinish()) {
            return;
        }
        setContentView(R.layout.customer_booking);
        // initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(CustomerBookingViewModel.class);

        initViews(); // initializing the views
        setupBottomSheetBehavior(); // setting up the bottom sheet
        setupHandleTouchControl(); // setting up the handle for the bottom sheet
        calculatePeekHeightAndOffsets(); // calculating the peek height and offsets for the bottom sheet
        setupButtonClick(); // setting up the select button for booking
        setupEditButton(); // setting up the edit button for search function
        applySystemUIHandling(); // setting up the system UI handling
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermissionIfNeeded();


        viewModel.getStartTimeMillis().observe(this, millis -> {
            if (millis == null) return;

            Date d = new Date(millis);
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM", Locale.UK);
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.UK);

            txtDate.setText(dateFmt.format(d));
            txtTime.setText("After " + timeFmt.format(d));
        });

        viewModel.getGuests().observe(this, g -> {
            if (g == null) return;
            txtGuests.setText("Table for " + g);
        });

        viewModel.getCity().observe(this, c -> {
            if (c == null || c.isEmpty()) return;
            txtCity.setText(c);
        });


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
            editButton.setOnClickListener(v -> {showSearchFilterDialog(); requestLocationPermissionIfNeeded();});
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

        EditText inputDate = dialogView.findViewById(R.id.input_date);
        EditText inputGuests = dialogView.findViewById(R.id.input_guests);

        builder.setView(dialogView);

        //set up date and time picker
        inputDate.setOnClickListener(v -> pickDateAndTime(inputDate));
        //the positive button for the pop up dialog to submit a search
        builder.setPositiveButton("Search", (dialog, which) -> {

            // guests
            // get the guest number input text
            String guestsStr = safeText(inputGuests);
            int g = 2;
            // check if it is empty or larger than max capacity
            try {
                if (!guestsStr.isEmpty()) g = Integer.parseInt(guestsStr);
            } catch (NumberFormatException ignored) {}

            if (g > 10) {
                Toast.makeText(this, "Maximum 10 guests", Toast.LENGTH_SHORT).show();
                g = 10;
            }

            viewModel.setGuests(g);


            // date and time
            Long millis = viewModel.getStartTimeMillis().getValue();
            // check if date and time are selected
            if (millis == null) {
                Toast.makeText(this, "Please select date & time", Toast.LENGTH_SHORT).show();
                return;
            }
            // convert the milli seconds to timestamp firebase format
            com.google.firebase.Timestamp requestedAfter =
                    new com.google.firebase.Timestamp(new Date(millis));

            // location that comes from the textview
            String locationText = txtCity.getText() == null ? "" : txtCity.getText().toString().trim();
            // check if location is empty
            if (locationText.isEmpty() || locationText.equalsIgnoreCase("Location Permission Required")) {
                Toast.makeText(this, "Location is required please select a location or allow location access", Toast.LENGTH_SHORT).show();
                return;
            }

            // run the search
            RestaurantRepository repo = new RestaurantRepository(/* context only if your constructor requires it */);

            Toast.makeText(this, "Searching for: " + locationText + ", guests: " + g + ", after: " + new SimpleDateFormat("HH:mm", Locale.UK).format(new Date(millis)), Toast.LENGTH_LONG).show();

            repo.searchAvailableRestaurants(
                    locationText,
                    requestedAfter,
                    g,
                    20,
                    new RestaurantRepository.OnAvailabilitySearchListener() {
                        @Override
                        public void onSuccess(List<com.example.dontjusteat.models.RestaurantAvailability> results) {
                            // later I will update the buttom sheet here
                            viewModel.setAvailabilityResults(results);

                            if (results.isEmpty()) {
                                Toast.makeText(customer_booking.this, "No availability found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            android.util.Log.e("BOOKING_DEBUG", "Search error: " + error);
                            Toast.makeText(customer_booking.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
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
                                Calendar chosen = Calendar.getInstance();
                                chosen.set(Calendar.YEAR, year);
                                chosen.set(Calendar.MONTH, month);
                                chosen.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                chosen.set(Calendar.HOUR_OF_DAY, hour);
                                chosen.set(Calendar.MINUTE, minute);
                                chosen.set(Calendar.SECOND, 0);
                                chosen.set(Calendar.MILLISECOND, 0);

                                long millis = chosen.getTimeInMillis();
                                viewModel.setStartTimeMillis(millis);

                                //show in the dialog input field
                                String display = dayOfMonth + "/" + (month + 1) + " after " + String.format("%02d:%02d", hour, minute);
                                inputDate.setText(display);
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


    //Permission functions
    private void requestLocationPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchUserLocation();
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION
            );
        }
    }

    // fetch the user's location
    private void fetchUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // fetch the last known location
        locationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        // no cached location yet
                        Toast.makeText(this, "Location not available. Turn on GPS and try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // get the location coordinates
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    // update the view model with the location
                    viewModel.setUserLocation(lat, lng);

                    // Resolve city name and display it
                    resolveCityFromLatLng(lat, lng);

//                    Toast.makeText(this, "Lat: " + lat + " Lng: " + lng, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                );

    }

    @Override
    // handle the result of the permission request
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resolveCityFromLatLng(double lat, double lng) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.UK);
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

                String city = "Location Permission Required";
                if (addresses != null && !addresses.isEmpty()) {
                    Address a = addresses.get(0);

                    if (a.getLocality() != null) {
                        city = a.getLocality();
                    } else if (a.getSubAdminArea() != null) {
                        city = a.getSubAdminArea();
                    } else if (a.getAdminArea() != null) {
                        city = a.getAdminArea();
                    }
                }

                final String finalCity = city;
                runOnUiThread(() -> askBookingCityOverride(finalCity));

            } catch (IOException e) {
                runOnUiThread(() -> txtCity.setText("Location Permission Required"));
            }
        });
    }

    private void askBookingCityOverride(String detectedCity) {
        new AlertDialog.Builder(this)
                .setTitle("Booking location")
                .setMessage("Are you booking within " + detectedCity + "?")
                .setPositiveButton("Yes", (d, w) -> {
                    // Use detected city
                    txtCity.setText(detectedCity);
                    viewModel.setCity(detectedCity);
                })
                .setNegativeButton("No", (d, w) -> promptForManualLocation(detectedCity))
                .show();
    }

    private void promptForManualLocation(String detectedCity) {
        final EditText input = new EditText(this);
        input.setHint("Enter city/area/postcode (e.g. Plymouth, PL1)");

        new AlertDialog.Builder(this)
                .setTitle("Enter booking location")
                .setMessage("Detected: " + detectedCity)
                .setView(input)
                .setPositiveButton("Use", (d, w) -> {
                    String manual = input.getText() == null ? "" : input.getText().toString().trim();
                    if (manual.isEmpty()) {
                        Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    txtCity.setText(manual);
                    viewModel.setCity(manual);
                })
                .setNegativeButton("Cancel", (d, w) -> {
                    // Keep detected city if they cancel
                    txtCity.setText(detectedCity);
                    viewModel.setCity(detectedCity);
                })
                .show();
    }





}
