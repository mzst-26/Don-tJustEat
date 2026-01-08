package com.example.dontjusteat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.dontjusteat.notifications.LocalNotificationHelper;
import com.example.dontjusteat.repositories.AdminBookingRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class admin_dashboard extends BaseActivity {
    private final Set<String> notifiedUrgents = new HashSet<>();
    private final com.example.dontjusteat.repositories.AdminBookingRepository adminRepoUrgent = new com.example.dontjusteat.repositories.AdminBookingRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // ensure only admins can view this screen
        if (!requireAdminOrFinish()) {
            return;
        }
        setContentView(R.layout.admin_dashboard);
        Modules.applyWindowInsets(this, R.id.rootView);

        ImageView back_button = findViewById(R.id.back_button);
        //remove this button for this page
        back_button.setVisibility(ImageView.GONE);

        // bind header info (name + image)
        bindHeaderInfo();

        // load real bookings from firestore
        loadBookingsFromFirestore();

        // start urgent listener
        startUrgentListener();

        // handle menu navigation
        admin_modules.handleMenuNavigation(this);

        //handle manage menu button
        manageMenueButtonHandler();
    }

    // load urgent and today bookings from firestore
    private void loadBookingsFromFirestore() {
        AdminBookingRepository repo = new AdminBookingRepository();

        final List<AdminBookingRepository.BookingModel> urgentHolder = new ArrayList<>();
        final List<AdminBookingRepository.BookingModel> todayHolder = new ArrayList<>();
        final boolean[] completed = {false, false};

        repo.loadAdminBookings(new AdminBookingRepository.OnBookingsLoadListener() {
            @Override
            public void onUrgentLoaded(List<AdminBookingRepository.BookingModel> urgent) {
                urgentHolder.addAll(urgent);
                completed[0] = true;
                if (completed[1]) {
                    mergeAndRender(urgentHolder, todayHolder);
                }
            }

            @Override
            public void onTodayLoaded(List<AdminBookingRepository.BookingModel> today) {
                todayHolder.addAll(today);
                completed[1] = true;
                if (completed[0]) {
                    mergeAndRender(urgentHolder, todayHolder);
                }
            }

            @Override
            public void onFailure(String error) {
                populateBookings(new ArrayList<>());
            }
        });
    }

    private void mergeAndRender(List<AdminBookingRepository.BookingModel> urgent,
                                List<AdminBookingRepository.BookingModel> today) {
        List<Booking> allBookings = new ArrayList<>();

        for (AdminBookingRepository.BookingModel bm : urgent) {
            allBookings.add(bookingModelToCard(bm));
        }
        for (AdminBookingRepository.BookingModel bm : today) {
            allBookings.add(bookingModelToCard(bm));
        }

        populateBookings(allBookings);
    }

    // convert repository model to UI card model
    private Booking bookingModelToCard(AdminBookingRepository.BookingModel bm) {
        // map status for UI display
        String displayStatus = bm.status;
        if (bm.status != null) {
            String s = bm.status.toUpperCase();
            if (s.equals("PENDING")) {
                displayStatus = "PENDING";
            } else if (s.equals("CONFIRMED")) {
                displayStatus = "CONFIRMED";
            } else if (s.equals("CHANGE REQUEST") || s.equals("REQUESTED CHANGE")) {
                displayStatus = "REQUESTED CHANGE";
            } else if (s.equals("CANCELLED BY STAFF")) {
                displayStatus = "CANCELLED BY STAFF";
            } else if (s.contains("CANCEL")) {
                displayStatus = "CANCELLED";
            }
        }

        return new Booking(
                bm.customerName,
                bm.tableId.isEmpty() ? "Table N/A" : "Table " + bm.tableId,
                bm.time,
                bm.date,
                bm.guests,
                bm.bookingId,
                displayStatus,
                bm.restaurantId,
                bm.customerPhone,
                R.drawable.logo,
                bm.acknowledgedByStaff
        );
    }

    //inflate cards into the scroll content container based on data
    private void populateBookings(List<Booking> bookings) {

        LinearLayout upcomingContentContainer = findViewById(R.id.todays_bookings_container);
        LinearLayout quickActionsToTakeContainer = findViewById(R.id.actions_to_take_container);
        LinearLayout canceledContainer = findViewById(R.id.cancelled_bookings_container);

        LinearLayout parentQuickActionsToTakeContainer = findViewById(R.id.parent_actions_to_take_container);

        if (upcomingContentContainer == null) return;

        upcomingContentContainer.removeAllViews();
        canceledContainer.removeAllViews();
        if (quickActionsToTakeContainer != null) {
            quickActionsToTakeContainer.removeAllViews();
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        int quickActionsCount = 0;

        // group by date for upcoming section
        java.util.Map<String, List<Booking>> upcomingByDate = new java.util.LinkedHashMap<>();
        List<Booking> urgentList = new ArrayList<>();
        List<Booking> canceledList = new ArrayList<>();

        for (Booking b : bookings) {
            boolean isCancel = b.status.equals("CANCELLED") || b.status.equals("CANCELLED BY STAFF");
            boolean isUnacknowledged = !b.acknowledgedByStaff;
            // only unacknowledged CANCELLED (by customer) go to urgent, not CANCELLED BY STAFF
            boolean needsQuickAction = b.status.equals("PENDING") || b.status.equals("REQUESTED CHANGE") || (b.status.equals("CANCELLED") && isUnacknowledged);

            if (isCancel) {
                canceledList.add(b);
            }
            if (needsQuickAction) {
                urgentList.add(b);
                quickActionsCount++;
            } else if (!isCancel) {
                // group upcoming by date
                upcomingByDate.computeIfAbsent(b.date, k -> new ArrayList<>()).add(b);
            }
        }

        // render urgent section
        for (Booking b : urgentList) {
            View card = inflater.inflate(R.layout.admin_component_dashboard_todays_booking_card, quickActionsToTakeContainer, false);
            bindBookingCard(card, b);
            card.setOnClickListener(v -> handleQuickActionClick(b));
            quickActionsToTakeContainer.addView(card);
        }

        // render cancelled bookings section
        if (!canceledList.isEmpty()) {
            TextView header = new TextView(this);
            header.setText("CANCELED BOOKINGS");
            header.setTextSize(14);
            header.setTypeface(null, android.graphics.Typeface.BOLD);
            header.setTextColor(android.graphics.Color.RED);
            header.setPadding(16, 16, 16, 8);
            canceledContainer.addView(header);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
            ));
            divider.setBackgroundColor(android.graphics.Color.RED);
            canceledContainer.addView(divider);

            // group canceled by date
            java.util.Map<String, List<Booking>> canceledByDate = new java.util.LinkedHashMap<>();
            for (Booking b : canceledList) {
                canceledByDate.computeIfAbsent(b.date, k -> new ArrayList<>()).add(b);
            }

            // render each date group
            for (String date : canceledByDate.keySet()) {
                View dateHeaderView = inflater.inflate(android.R.layout.simple_list_item_1, canceledContainer, false);
                TextView dateHeaderText = dateHeaderView.findViewById(android.R.id.text1);
                dateHeaderText.setText(date);
                dateHeaderText.setTextSize(12);
                dateHeaderText.setTypeface(null, android.graphics.Typeface.BOLD);
                dateHeaderText.setTextColor(android.graphics.Color.RED);
                dateHeaderText.setPadding(16, 8, 16, 4);
                canceledContainer.addView(dateHeaderView);

                List<Booking> dateBookings = canceledByDate.get(date);
                for (Booking b : dateBookings) {
                    View card = inflater.inflate(R.layout.admin_component_dashboard_todays_booking_card, canceledContainer, false);
                    bindBookingCard(card, b);
                    card.setOnClickListener(v -> handleQuickActionClick(b));
                    canceledContainer.addView(card);
                }
            }
        }

        // render upcoming section with date headers
        for (String date : upcomingByDate.keySet()) {
            // add date header
            View headerView = inflater.inflate(android.R.layout.simple_list_item_1, upcomingContentContainer, false);
            TextView headerText = headerView.findViewById(android.R.id.text1);
            headerText.setText(date);
            headerText.setTextSize(14);
            headerText.setTypeface(null, android.graphics.Typeface.BOLD);
            headerText.setTextColor(android.graphics.Color.BLACK);
            headerText.setPadding(16, 16, 16, 8);
            upcomingContentContainer.addView(headerView);

            // add divider line under date
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
            ));
            divider.setBackgroundColor(android.graphics.Color.BLACK);
            upcomingContentContainer.addView(divider);

            // add bookings for this date
            List<Booking> dateBookings = upcomingByDate.get(date);
            for (Booking b : dateBookings) {
                View card = inflater.inflate(R.layout.admin_component_dashboard_todays_booking_card, upcomingContentContainer, false);
                bindBookingCard(card, b);
                card.setOnClickListener(v -> handleBookingDetailsClick(b));
                upcomingContentContainer.addView(card);
            }
        }

        // hide urgent section if empty
        if (parentQuickActionsToTakeContainer != null) {
            parentQuickActionsToTakeContainer.setVisibility(quickActionsCount > 0 ? View.VISIBLE : View.GONE);
        }
    }

    // helper to bind booking card data
    private void bindBookingCard(View card, Booking b) {
        ImageView avatar = card.findViewById(R.id.booking_avatar);
        TextView name = card.findViewById(R.id.booking_name);
        TextView tableTime = card.findViewById(R.id.booking_table_time);
        TextView status = card.findViewById(R.id.booking_status);

        avatar.setImageResource(b.avatarResId);
        name.setText(b.name);
        tableTime.setText(b.tableLabel + " • " + b.time);
        status.setText(b.status);
    }

    // Handle click on quick action cards (New Request, Requested Change, Canceled)
    private void handleQuickActionClick(Booking booking) {
        showActionPopup(booking);
    }

    // Handle click on today's booking cards (regular bookings)
    private void handleBookingDetailsClick(Booking booking) {
        showBookingDetailsPopup(booking);
    }

    // Show popup dialog for booking actions
    private void showActionPopup(Booking booking) {
        // Create dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View popupView = LayoutInflater.from(this).inflate(R.layout.admin_component_booking_detail_popup, null);
        builder.setView(popupView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Bind data to popup
        TextView requestType = popupView.findViewById(R.id.popup_request_type);
        ImageView avatar = popupView.findViewById(R.id.popup_customer_avatar);
        TextView customerName = popupView.findViewById(R.id.popup_customer_name);
        TextView customerInfo = popupView.findViewById(R.id.popup_customer_info);
        TextView tableNumber = popupView.findViewById(R.id.popup_table_number);
        TextView time = popupView.findViewById(R.id.popup_time);
        TextView date = popupView.findViewById(R.id.popup_date);
        TextView partySize = popupView.findViewById(R.id.popup_party_size);
        LinearLayout notesContainer = popupView.findViewById(R.id.popup_notes_container);

        // Set data
        String requestTypeText = booking.status;
        requestType.setText(requestTypeText);

        avatar.setImageResource(booking.avatarResId);
        customerName.setText(booking.name);
        String customerInfoText = "Booking ID: #" + booking.bookingId;
        if (booking.phone != null && !booking.phone.isEmpty()) {
            customerInfoText += "\nPhone: " + booking.phone;
        }
        // show admin name if cancelled by staff
        if (booking.status.equals("CANCELLED BY STAFF")) {
            customerInfoText += "\nCancelled by: Admin";
        }
        customerInfo.setText(customerInfoText);
        tableNumber.setText(booking.tableLabel);
        time.setText(booking.time);
        date.setText(booking.date);
        partySize.setText(booking.guests + " guests");

        // Hide notes if empty (you can add notes field to Booking class later)
        notesContainer.setVisibility(View.GONE);

        // Action buttons
        android.widget.Button rejectButton = popupView.findViewById(R.id.popup_reject_button);
        android.widget.Button acceptButton = popupView.findViewById(R.id.popup_accept_button);

        //for cancellation requests only show the accept button
        if (booking.status.equals("CANCELLED")) {
            rejectButton.setVisibility(View.GONE);
            // hide button if already acknowledged
            if (booking.acknowledgedByStaff) {
                acceptButton.setVisibility(View.GONE);
            } else {
                acceptButton.setText("Acknowledge Cancellation");
                //make accept button full width
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) acceptButton.getLayoutParams();
                params.setMarginStart(0);
                acceptButton.setLayoutParams(params);
            }
        } else if (booking.status.equals("CANCELLED BY STAFF")) {
            // admin cancelled - show no buttons
            rejectButton.setVisibility(View.GONE);
            acceptButton.setVisibility(View.GONE);
        } else {
            // PENDING or REQUESTED CHANGE - show reject/accept buttons
            rejectButton.setVisibility(View.VISIBLE);
            rejectButton.setText("Cancel");
            rejectButton.setOnClickListener(v -> {
                handleRejectBooking(booking);
                dialog.dismiss();
            });
            acceptButton.setText("Accept");
        }

        acceptButton.setOnClickListener(v -> {
            handleAcceptBooking(booking);
            dialog.dismiss();
        });

        dialog.show();
    }

    //handle accepting a booking request
    private void handleAcceptBooking(Booking booking) {
        AdminBookingRepository repo = new AdminBookingRepository();

        boolean isCancellation = booking.status.equals("CANCELLED");
        String newStatus = isCancellation ? booking.status : "CONFIRMED";
        boolean acknowledge = isCancellation;

        repo.updateBookingStatus(booking.restaurantId, booking.bookingId, newStatus, acknowledge,
                new AdminBookingRepository.OnStatusUpdateListener() {
                    @Override
                    public void onSuccess() {
                        String message = isCancellation ?
                                "Cancellation acknowledged" :
                                "Booking confirmed: " + booking.name;
                        android.widget.Toast.makeText(admin_dashboard.this, message,
                                android.widget.Toast.LENGTH_SHORT).show();

                        // refresh immediately
                        loadBookingsFromFirestore();
                    }

                    @Override
                    public void onFailure(String error) {
                        android.widget.Toast.makeText(admin_dashboard.this,
                                "Failed to update: " + error,
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Handle rejecting a booking request
    private void handleRejectBooking(Booking booking) {
        AdminBookingRepository repo = new AdminBookingRepository();

        // when admin cancels, auto-acknowledge it
        repo.updateBookingStatus(booking.restaurantId, booking.bookingId, "CANCELLED BY STAFF", true,
                new AdminBookingRepository.OnStatusUpdateListener() {
                    @Override
                    public void onSuccess() {
                        android.widget.Toast.makeText(admin_dashboard.this,
                                "Booking cancelled: " + booking.name,
                                android.widget.Toast.LENGTH_SHORT).show();

                        // refresh immediately
                        loadBookingsFromFirestore();
                    }

                    @Override
                    public void onFailure(String error) {
                        android.widget.Toast.makeText(admin_dashboard.this,
                                "Failed to update: " + error,
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // show booking details popup
    //This is used for regular bookings that are already confirmed
    private void showBookingDetailsPopup(Booking booking) {
        //create a dialog popup
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View popupView = LayoutInflater.from(this).inflate(R.layout.admin_component_booking_detail_popup, null);
        builder.setView(popupView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        //get all the text fields from the popup
        TextView requestType = popupView.findViewById(R.id.popup_request_type);
        ImageView avatar = popupView.findViewById(R.id.popup_customer_avatar);
        TextView customerName = popupView.findViewById(R.id.popup_customer_name);
        TextView customerInfo = popupView.findViewById(R.id.popup_customer_info);
        TextView tableNumber = popupView.findViewById(R.id.popup_table_number);
        TextView time = popupView.findViewById(R.id.popup_time);
        TextView date = popupView.findViewById(R.id.popup_date);
        TextView partySize = popupView.findViewById(R.id.popup_party_size);
        LinearLayout notesContainer = popupView.findViewById(R.id.popup_notes_container);
        LinearLayout actionButtonsContainer = popupView.findViewById(R.id.popup_action_buttons_container);

        //Set the title to show this is just viewing details
        requestType.setText("Booking Details");

        // Fiill in all the booking information
        avatar.setImageResource(booking.avatarResId);
        customerName.setText(booking.name);
        String customerInfoText = "Booking ID: #" + booking.bookingId;
        if (booking.phone != null && !booking.phone.isEmpty()) {
            customerInfoText += "\nPhone: " + booking.phone;
        }
        // show admin name if cancelled by staff
        if (booking.status.equals("CANCELLED BY STAFF")) {
            customerInfoText += "\nCancelled by: Admin";
        }
        customerInfo.setText(customerInfoText);
        tableNumber.setText(booking.tableLabel);
        time.setText(booking.time);
        date.setText(booking.date);
        partySize.setText(booking.guests + " guests");


        //Hide notes section (not needed for now)
        notesContainer.setVisibility(View.GONE);


        // hide the action buttons so users can only view, not modify
        if (actionButtonsContainer != null) {
            actionButtonsContainer.setVisibility(View.GONE);
        }

        // let users close the popup by tapping outside of it
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    //bind header info (name + image) from restaurant for this admin
    private void bindHeaderInfo() {
        TextView headerTitle = findViewById(R.id.header_location_name);
        ImageView headerImage = findViewById(R.id.right_header_image);

        // fetch restaurant for this admin and update header
        com.example.dontjusteat.repositories.AdminBookingRepository adminRepo = new com.example.dontjusteat.repositories.AdminBookingRepository();
        adminRepo.getAdminRestaurantId(new com.example.dontjusteat.repositories.AdminBookingRepository.OnAdminRestaurantListener() {
            @Override
            public void onSuccess(String rid) {
                com.example.dontjusteat.repositories.RestaurantRepository repo = new com.example.dontjusteat.repositories.RestaurantRepository();
                repo.getRestaurantById(rid, new com.example.dontjusteat.repositories.RestaurantRepository.OnRestaurantFetchListener() {
                    @Override
                    public void onSuccess(com.example.dontjusteat.models.Restaurant r) {
                        if (headerTitle != null && r != null && r.getName() != null) {
                            headerTitle.setText(r.getName());
                        }
                        if (headerImage != null && r != null && r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
                            try {
                                com.bumptech.glide.Glide.with(admin_dashboard.this).load(r.getImageUrl()).into(headerImage);
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        android.util.Log.e("ADMIN_DASH", "Header load failed: " + error);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("ADMIN_DASH", "No restaurant id for header: " + error);
            }
        });
    }

    //handle manage menu button
    private void manageMenueButtonHandler() {
        Button manageMenuButton = findViewById(R.id.manage_menu_button);
        manageMenuButton.setOnClickListener(v -> {
            // navigate to the admin menu management page
            android.content.Intent intent = new android.content.Intent(this, admin_manage_menu.class);
            startActivity(intent);
        });
    }

    private void startUrgentListener() {
        com.example.dontjusteat.repositories.AdminBookingRepository repo = new com.example.dontjusteat.repositories.AdminBookingRepository();
        repo.getAdminRestaurantId(new com.example.dontjusteat.repositories.AdminBookingRepository.OnAdminRestaurantListener() {
            @Override

            // start listening for urgent bookings
            public void onSuccess(String rid) {

                // listen for urgent bookings and push local notification
                adminRepoUrgent.listenForUrgentActions(rid, (title, msg) -> {
                    String key = title + msg;
                    // avoid duplicate notifications
                    if (!notifiedUrgents.contains(key)) {
                        notifiedUrgents.add(key);
                        LocalNotificationHelper.notifyNow(admin_dashboard.this, title, msg);
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("ADMIN_DASH", "Urgent listener failed: " + error);
            }
        });
    }

    // Data model for a booking
    static class Booking {
        String name;
        String tableLabel;
        String time;
        String date;
        int guests;
        String bookingId;
        String status;
        String restaurantId;
        String phone;
        int avatarResId;
        boolean acknowledgedByStaff;
        String adminName;

        Booking(String name, String tableLabel, String time, String date, int guests, String bookingId, String status, String restaurantId, String phone, int avatarResId, boolean acknowledgedByStaff) {
            this.name = name;
            this.tableLabel = tableLabel;
            this.time = time;
            this.date = date;
            this.guests = guests;
            this.bookingId = bookingId;
            this.status = status;
            this.restaurantId = restaurantId;
            this.phone = phone;
            this.avatarResId = avatarResId;
            this.acknowledgedByStaff = acknowledgedByStaff;
            this.adminName = "";
        }
    }
}
