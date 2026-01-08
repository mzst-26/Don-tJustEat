package com.example.dontjusteat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.dontjusteat.models.Restaurant;
import com.example.dontjusteat.models.Table;
import com.example.dontjusteat.models.TableAvailability;
import com.example.dontjusteat.notifications.LocalNotificationHelper;
import com.example.dontjusteat.repositories.BookingRepository;
import com.example.dontjusteat.repositories.RestaurantRepository;
import com.example.dontjusteat.repositories.UserProfileRepository;
import com.example.dontjusteat.viewMode.CustomerBookingViewModel;
import com.google.android.material.slider.Slider;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class booking_summary extends BaseActivity {

    private String restaurantId;
    private int partySize;
    private CustomerBookingViewModel viewModel;
    private RestaurantRepository repo;
    private BookingRepository bookingRepo;
    private Restaurant currentRestaurant;  // store restaurant for duration config

    // currently selected table
    private Table currentTable;
    private final List<Table> availableTables = new ArrayList<>();
    private final Map<String, List<String>> tableTimesMap = new HashMap<>(); // tableId -> list of time strings


    //ui components
    private TextView tvTableName;
    private TextView tvTableCapacity;
    private Slider sliderTable;
    private TextView tvDiningTime;
    private Slider sliderDiningTime;
    private LinearLayout selectedTablesContainer;
    private Button confirmBookingButton;
    private Button selectTableButton;

    // track selected tables and times
    private final Map<String, String> selectedTableTimes = new HashMap<>(); // tableId -> selected time string

    // store slot objects for timestamp reconstruction
    private final Map<String, List<com.example.dontjusteat.models.Slot>> tableSlots = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireCustomerOrFinish()) {
            return;
        }
        setContentView(R.layout.booking_summary);

        // get data from intent
        restaurantId = getIntent().getStringExtra("restaurantId");
        partySize = getIntent().getIntExtra("partySize", 2);

        if (restaurantId == null || restaurantId.isEmpty()) {
            Toast.makeText(this, "Restaurant ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(CustomerBookingViewModel.class);
        repo = new RestaurantRepository();
        bookingRepo = new BookingRepository();

        // Initialize UI
        initializeViews();
        initializeNavigationService();

        //load user data dynamically
        loadUserData();

        // load tables and availability
        loadTablesForRestaurant();

        Modules.applyWindowInsets(this, R.id.rootView);
    }

    private void initializeViews() {
        tvTableName = findViewById(R.id.tv_table_name);
        tvTableCapacity = findViewById(R.id.tv_table_capacity);
        sliderTable = findViewById(R.id.slider_table_selection);
        tvDiningTime = findViewById(R.id.tv_dining_time);
        sliderDiningTime = findViewById(R.id.slider_dining_time);
        selectedTablesContainer = findViewById(R.id.selected_tables_container);
        confirmBookingButton = findViewById(R.id.confirm_booking_button);
        selectTableButton = findViewById(R.id.select_table_button);

        selectTableButton.setOnClickListener(v -> selectCurrentTable());
        confirmBookingButton.setOnClickListener(v -> confirmBooking());

        //add dining time slider listener once (prevents stacking on table changes)
        sliderDiningTime.addOnChangeListener((slider, value, fromUser) -> {
            List<String> times = tableTimesMap.getOrDefault(currentTable.getId(), new ArrayList<>());
            int index = (int) value;
            if (index >= 0 && index < times.size()) {
                tvDiningTime.setText(times.get(index));
            }
        });
    }

    private void initializeNavigationService() {
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
    }

    // Load tables that can fit the party size
    private void loadTablesForRestaurant() {
        // first fetch restaurant details to get duration/slot config
        repo.getRestaurantById(restaurantId, new RestaurantRepository.OnRestaurantFetchListener() {
            @Override
            public void onSuccess(Restaurant restaurant) {
                // store restaurant for booking creation
                currentRestaurant = restaurant;
                // display restaurant data in UI
                displayRestaurantData(restaurant);
                // load tables with configuration
                loadTablesWithConfig(restaurant);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(booking_summary.this, "Failed to load restaurant: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadTablesWithConfig(Restaurant restaurant) {
        repo.getTablesForRestaurant(restaurantId, new RestaurantRepository.OnTablesListener() {
            @Override
            public void onSuccess(List<Table> tables) {
                // filter tables by capacity
                availableTables.clear();
                for (Table t : tables) {
                    if (t.getCapacity() >= partySize) {
                        availableTables.add(t);
                    }
                }

                if (availableTables.isEmpty()) {
                    Toast.makeText(booking_summary.this, "No tables available for " + partySize + " guests", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                //load availability for all filtered tables
                List<String> tableIds = new ArrayList<>();
                for (Table t : availableTables) {
                    tableIds.add(t.getId());
                }

                long requestedAfterMsExtra = getIntent().getLongExtra("requestedAfterMs", -1);
                long requestedMs = requestedAfterMsExtra > 0 ? requestedAfterMsExtra : System.currentTimeMillis();
                Timestamp requestedAfter = new Timestamp(new Date(requestedMs));


                int duration = 90;
                int slotMin = 15;

                repo.getTableAvailability(
                        restaurantId,
                        tableIds,
                        requestedAfter,
                        duration,
                        slotMin,
                        new RestaurantRepository.OnTableAvailabilityListener() {
                            @Override
                            public void onSuccess(List<TableAvailability> results) {
                                //Build time strings map and store slot objects
                                tableTimesMap.clear();
                                tableSlots.clear();
                                for (TableAvailability ta : results) {
                                    List<String> times = new ArrayList<>();
                                    SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.UK);
                                    for (com.example.dontjusteat.models.Slot slot : ta.slots) {
                                        times.add(timeFmt.format(slot.startTime.toDate()));
                                    }
                                    tableTimesMap.put(ta.table.getId(), times);
                                    tableSlots.put(ta.table.getId(), ta.slots);
                                }

                                setupTableSlider();


                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(booking_summary.this, "Failed to load availability: " + error, Toast.LENGTH_SHORT).show();
                            }

                        }
                );
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(booking_summary.this, "Failed to load tables: " + error, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void setupTableSlider() {
        if (availableTables.isEmpty()) return;

        sliderTable.setValueFrom(0);
        sliderTable.setValueTo(availableTables.size() - 1);
        sliderTable.setStepSize(1);
        sliderTable.setValue(0);

        currentTable = availableTables.get(0);
        updateTableDisplay();

        sliderTable.setLabelFormatter(value -> {
            int index = (int) value;
            if (index >= 0 && index < availableTables.size()) {
                return availableTables.get(index).getDisplayName();
            }
            return "";
        });

        sliderTable.addOnChangeListener((slider, value, fromUser) -> {
            int index = (int) value;
            if (index >= 0 && index < availableTables.size()) {
                currentTable = availableTables.get(index);
                updateTableDisplay();
            }
        });
    }

    private void updateTableDisplay() {
        if (currentTable == null) return;

        tvTableName.setText(currentTable.getDisplayName());
        tvTableCapacity.setText("Capacity: " + currentTable.getCapacity() + " guests");

        //Update time slider for this table
        updateTimeSlider();


        //Show current selection status
        updateSelectionStatus();
    }

    private void updateTimeSlider() {
        if (currentTable == null) return;

        List<String> times = tableTimesMap.getOrDefault(currentTable.getId(), new ArrayList<>());

        if (times.isEmpty()) {
            // no times available - set valid range but disable interaction
            sliderDiningTime.setValueFrom(0);
            sliderDiningTime.setValueTo(1);
            sliderDiningTime.setStepSize(1);
            sliderDiningTime.setValue(0);
            sliderDiningTime.setEnabled(false);
            tvDiningTime.setText("No times available");
        } else {
            // when times available  set valid range
            sliderDiningTime.setEnabled(true);
            sliderDiningTime.setValueFrom(0);
            sliderDiningTime.setValueTo(times.size() - 1);
            sliderDiningTime.setStepSize(1);
            sliderDiningTime.setValue(0);
            tvDiningTime.setText(times.get(0));
        }

        sliderDiningTime.setLabelFormatter(value -> {
            int index = (int) value;
            if (index >= 0 && index < times.size()) {
                return times.get(index);
            }
            return "";

        });
    }

    // sllow selecting this table with current time
    private void selectCurrentTable() {
        if (currentTable == null) {
            Toast.makeText(this, "No table selected", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> times = tableTimesMap.getOrDefault(currentTable.getId(), new ArrayList<>());
        int timeIndex = (int) sliderDiningTime.getValue();

        if (timeIndex < 0 || timeIndex >= times.size()) {
            Toast.makeText(this, "No time selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // check active bookings limit before adding table
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "Please log in to make a booking", Toast.LENGTH_SHORT).show();
            return;
        }

        // check how many active bookings user has
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(userId).collection("bookings")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int activeCount = 0;
                    long currentTimeMs = System.currentTimeMillis();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = doc.getString("status");

                        if ("PENDING".equalsIgnoreCase(status)) {
                            activeCount++;
                        } else if ("CONFIRMED".equalsIgnoreCase(status)) {
                            com.google.firebase.Timestamp bookingStartTime = doc.getTimestamp("startTime");
                            if (bookingStartTime != null && bookingStartTime.toDate().getTime() > currentTimeMs) {
                                activeCount++;
                            }
                        }
                    }

                    // calculate max tables user can add
                    int maxTablesAllowed = 3 - activeCount;
                    int currentlySelected = selectedTableTimes.size();

                    if (currentlySelected >= maxTablesAllowed) {
                        String message = activeCount == 0 ?
                                "You can only select up to 3 tables per booking" :
                                "You have " + activeCount + " active booking(s). You can only have 3 active bookings at a time.";
                        Toast.makeText(booking_summary.this, message, Toast.LENGTH_LONG).show();
                        return;
                    }

                    // proceed with adding table
                    addTableToSelection(timeIndex, times);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check booking limit", Toast.LENGTH_SHORT).show();
                });
    }

    private void addTableToSelection(int timeIndex, List<String> times) {
        String selectedTime = times.get(timeIndex);
        selectedTableTimes.put(currentTable.getId(), selectedTime);

        // store timestamp for later booking creation
        List<com.example.dontjusteat.models.Slot> slots = tableSlots.get(currentTable.getId());
        if (slots != null && timeIndex < slots.size()) {
            com.google.firebase.Timestamp ts = slots.get(timeIndex).startTime;
            viewModel.selectTimeForTable(currentTable.getId(), ts);
        }

        updateSelectionStatus();

        Toast.makeText(this, currentTable.getDisplayName() + " at " + selectedTime + " selected", Toast.LENGTH_SHORT).show();
    }

    private void updateSelectionStatus() {
        selectedTablesContainer.removeAllViews();

        if (selectedTableTimes.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Select tables and times to proceed");
            empty.setTextColor(getResources().getColor(android.R.color.darker_gray));
            selectedTablesContainer.addView(empty);
            return;
        }

        for (Map.Entry<String, String> entry : selectedTableTimes.entrySet()) {
            String tableId = entry.getKey();
            String time = entry.getValue();

            // Find table by ID
            Table t = null;
            for (Table table : availableTables) {
                if (table.getId().equals(tableId)) {
                    t = table;
                    break;
                }
            }

            if (t != null) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(16, 8, 16, 8);

                TextView text = new TextView(this);
                text.setText(t.getDisplayName() + " - " + time);
                text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                row.addView(text);

                Button removeBtn = new Button(this);
                removeBtn.setText("Remove");
                removeBtn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                removeBtn.setOnClickListener(v -> {
                    selectedTableTimes.remove(tableId);
                    updateSelectionStatus();
                });
                row.addView(removeBtn);

                selectedTablesContainer.addView(row);
            }
        }
    }

    private void confirmBooking() {
        // validate selections
        if (selectedTableTimes.isEmpty()) {
            Toast.makeText(this, "Please select at least one table and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // get current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate restaurant and duration
        if (currentRestaurant == null) {
            Toast.makeText(this, "Restaurant data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }


        int durationMinutes = currentRestaurant.getDefaultDurationMinutes() > 0 ?
                currentRestaurant.getDefaultDurationMinutes() : 90;

        // show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating booking...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // disable button to prevent double submission
        confirmBookingButton.setEnabled(false);


        // create bookings for each selected table
        createBookingsForTables(userId, durationMinutes, progressDialog);
    }

    // create bookings for all selected tables
    private void createBookingsForTables(String userId, int durationMinutes, ProgressDialog progressDialog) {
        // counter to track completed bookings
        final int totalBookings = selectedTableTimes.size();
        final int[] completedBookings = {0};
        final boolean[] hasError = {false};
        final java.util.List<String> bookingIds = new java.util.ArrayList<>();


        for (Map.Entry<String, String> entry : selectedTableTimes.entrySet()) {
            String tableId = entry.getKey();
            String timeStr = entry.getValue();

            // get the actual timestamp for this table/time

            List<com.example.dontjusteat.models.Slot> slots = tableSlots.get(tableId);
            if (slots == null || slots.isEmpty()) {
                handleBookingError(progressDialog, "No time slots available for table " + tableId);
                return;
            }


            // find the matching slot by time string
            Timestamp startTime = null;
            for (com.example.dontjusteat.models.Slot slot : slots) {
                String slotTimeStr = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.UK)
                        .format(slot.startTime.toDate());
                if (slotTimeStr.equals(timeStr)) {
                    startTime = slot.startTime;
                    break;
                }
            }

            if (startTime == null) {
                handleBookingError(progressDialog, "Invalid time selection for table " + tableId);
                return;
            }


            // create booking
            final Timestamp finalStartTime = startTime;
            bookingRepo.createBooking(restaurantId, userId, tableId, finalStartTime,
                    durationMinutes, partySize,
                    new BookingRepository.OnBookingCreateListener() {
                        @Override
                        public void onSuccess(String bookingId) {
                            completedBookings[0]++;
                            bookingIds.add(bookingId);

                            // instant local notification with vibration when user creates booking
                            try {
                                LocalNotificationHelper.notifyNow(booking_summary.this,
                                        "Booking created",
                                        "Your booking #" + bookingId.substring(0, Math.min(8, bookingId.length())) + " was created");
                            } catch (Exception ignored) {
                            }

                            // after each booking success create a notification for the user (db)
                            try {
                                com.example.dontjusteat.repositories.NotificationRepository nRepo = new com.example.dontjusteat.repositories.NotificationRepository();
                                com.example.dontjusteat.models.Notification notif = new com.example.dontjusteat.models.Notification(
                                        "Booking created",
                                        "Your booking #" + bookingId.substring(0, Math.min(8, bookingId.length())) + " was created",
                                        "unread",
                                        com.google.firebase.Timestamp.now(),
                                        bookingId,
                                        restaurantId
                                );

                                nRepo.createNotification(userId, notif);
                            } catch (Exception ignored) {
                            }

                            // check if all bookings are done
                            if (completedBookings[0] == totalBookings && !hasError[0]) {
                                progressDialog.dismiss();
                                confirmBookingButton.setEnabled(true);

                                // navigate to confirmation page with booking IDs
                                Intent intent = new Intent(booking_summary.this, booking_confirmation.class);
                                intent.putExtra("restaurantId", restaurantId);
                                intent.putStringArrayListExtra("bookingIds", new ArrayList<>(bookingIds));
                                startActivity(intent);
                                finish();
                            }

                        }

                        @Override
                        public void onFailure(String error) {
                            if (!hasError[0]) {
                                hasError[0] = true;
                                handleBookingError(progressDialog, error);
                            }

                        }
                    });
        }
    }

    // handle booking creation errors
    private void handleBookingError(ProgressDialog progressDialog, String error) {
        progressDialog.dismiss();

        confirmBookingButton.setEnabled(true);
        Toast.makeText(this, "Booking failed: " + error, Toast.LENGTH_LONG).show();
    }

    // load current user data
    private void loadUserData() {
        UserProfileRepository userRepo = new UserProfileRepository(this);
        userRepo.loadUserProfileWithEmail(new UserProfileRepository.OnProfileWithEmailLoadListener() {
            @Override
            public void onSuccess(String name, String email, String phone) {
                displayUserData(name, email, phone);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(booking_summary.this, "Failed to load user data: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // display user data in UI
    private void displayUserData(String name, String email, String phone) {

        TextView tvName = findViewById(R.id.name);
        TextView tvEmail = findViewById(R.id.email);
        TextView tvPhone = findViewById(R.id.phone_number);


        if (tvName != null) tvName.setText(name != null ? name : "N/A");
        if (tvEmail != null) tvEmail.setText(email != null ? email : "N/A");
        if (tvPhone != null) tvPhone.setText(phone != null ? phone : "");
    }


    // display restaurant data in header and location section
    private void displayRestaurantData(Restaurant restaurant) {
        // set header title with restaurant name
        TextView headerTitle = findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(restaurant.getName() != null ? restaurant.getName() : "Restaurant");
        }


        // set location name
        TextView tvLocationName = findViewById(R.id.location_name);
        if (tvLocationName != null) {
            tvLocationName.setText(restaurant.getName() != null ? restaurant.getName() : "N/A");
        }


        // set location address
        TextView tvLocationAddress = findViewById(R.id.location_address);
        if (tvLocationAddress != null) {
            tvLocationAddress.setText(restaurant.getAddress() != null ? restaurant.getAddress() : "N/A");
        }


        // setup map link button
        Button btnMapLink = findViewById(R.id.location_map_link);
        if (btnMapLink != null) {
            btnMapLink.setOnClickListener(v -> openLocationOnMap(restaurant.getLocationURL()));
        }
    }

    // open location on map using locationURL
    private void openLocationOnMap(String locationURL) {
        if (locationURL == null || locationURL.trim().isEmpty()) {
            Toast.makeText(this, "Location URL not available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // validate and parse URI
            Uri locationUri = Uri.parse(locationURL.trim());


            // try opening with maps app first
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, locationUri);
            mapIntent.setPackage("com.google.android.apps.maps");


            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {

                // fallback to browser if maps not available
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, locationUri);
                if (browserIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(this, "No app available to view location", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            Toast.makeText(this, "Invalid location URL", Toast.LENGTH_SHORT).show();
        }
    }
}
