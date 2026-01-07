package com.example.dontjusteat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dontjusteat.repositories.AdminBookingRepository;

import java.util.ArrayList;
import java.util.List;

public class admin_dashboard extends BaseActivity {
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

        // load real bookings from firestore
        loadBookingsFromFirestore();

        // handle menu navigation
        admin_modules.handleMenuNavigation(this);

        //handle manage menu button
        manageMenueButtonHandler();
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

        Booking(String name, String tableLabel, String time, String date, int guests, String bookingId, String status, String restaurantId, String phone, int avatarResId) {
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
        }
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
        if (bm.status != null && bm.status.equalsIgnoreCase("PENDING")) {
            displayStatus = "New Request";
        } else if (bm.status != null && bm.status.equalsIgnoreCase("CHANGE REQUEST")) {
            displayStatus = "Requested Change";
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
                R.drawable.logo
        );
    }


    //inflate cards into the scroll content container based on data
    private void populateBookings(List<Booking> bookings) {

        LinearLayout upcomingContentContainer = findViewById(R.id.todays_bookings_container);
        LinearLayout quickActionsToTakeContainer = findViewById(R.id.actions_to_take_container);

        LinearLayout parentQuickActionsToTakeContainer = findViewById(R.id.parent_actions_to_take_container);

        if (upcomingContentContainer == null) return;

        upcomingContentContainer.removeAllViews();
        if (quickActionsToTakeContainer != null) {
            quickActionsToTakeContainer.removeAllViews();
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        int quickActionsCount = 0;

        // group by date for upcoming section
        java.util.Map<String, List<Booking>> upcomingByDate = new java.util.LinkedHashMap<>();
        List<Booking> urgentList = new ArrayList<>();

        for (Booking b : bookings) {
            boolean needsQuickAction = b.status.equals("New Request") ||
                                      b.status.equals("Requested Change") ||
                                      b.status.equals("Canceled");

            if (needsQuickAction) {
                urgentList.add(b);
                quickActionsCount++;
            } else {
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

        // render upcoming section with date headers
        for (String date : upcomingByDate.keySet()) {
            // add date header
            View headerView = inflater.inflate(android.R.layout.simple_list_item_1, upcomingContentContainer, false);
            TextView headerText = headerView.findViewById(android.R.id.text1);
            headerText.setText(date);
            headerText.setTextSize(14);
            headerText.setTypeface(null, android.graphics.Typeface.BOLD);
            headerText.setPadding(16, 16, 16, 8);
            upcomingContentContainer.addView(headerView);

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
        if (booking.status.equals("New Request")) {
            requestTypeText = "New Booking Request";
        } else if (booking.status.equals("Requested Change")) {
            requestTypeText = "Booking Change Request";
        } else if (booking.status.equals("Canceled")) {
            requestTypeText = "Cancellation Request";
        }
        requestType.setText(requestTypeText);

        avatar.setImageResource(booking.avatarResId);
        customerName.setText(booking.name);
        String customerInfoText = "Booking ID: #" + booking.bookingId;
        if (booking.phone != null && !booking.phone.isEmpty()) {
            customerInfoText += "\nPhone: " + booking.phone;
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
        if (booking.status.equals("Canceled")) {
            rejectButton.setVisibility(View.GONE);
            acceptButton.setText("Acknowledge Cancellation");
            //make accept button full width
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) acceptButton.getLayoutParams();
            params.setMarginStart(0);
            acceptButton.setLayoutParams(params);
        } else {
            rejectButton.setVisibility(View.VISIBLE);
            rejectButton.setOnClickListener(v -> {
                handleRejectBooking(booking);
                dialog.dismiss();
            });
        }

        acceptButton.setOnClickListener(v -> {
            handleAcceptBooking(booking);
            dialog.dismiss();
        });

        dialog.show();
    }

    //handle accepting a booking request
    private void handleAcceptBooking(Booking booking) {
        // disable button during update
        AdminBookingRepository repo = new AdminBookingRepository();

        // check if this is a cancellation (acknowledge) or a new booking (accept)
        boolean isCancellation = booking.status.equals("Canceled");
        String newStatus = isCancellation ? booking.status : "CONFIRMED";
        boolean acknowledge = isCancellation;

        repo.updateBookingStatus(booking.restaurantId, booking.bookingId, newStatus, acknowledge,
                new AdminBookingRepository.OnStatusUpdateListener() {
                    @Override
                    public void onSuccess() {
                        String message = isCancellation ?
                                "Cancellation acknowledged" :
                                "Booking accepted: " + booking.name;
                        android.widget.Toast.makeText(admin_dashboard.this, message,
                                android.widget.Toast.LENGTH_SHORT).show();

                        // refresh booking lists
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

        repo.updateBookingStatus(booking.restaurantId, booking.bookingId, "REJECTED BY STAFF", false,
                new AdminBookingRepository.OnStatusUpdateListener() {
                    @Override
                    public void onSuccess() {
                        android.widget.Toast.makeText(admin_dashboard.this,
                                "Booking rejected: " + booking.name,
                                android.widget.Toast.LENGTH_SHORT).show();

                        // refresh booking lists
                        loadBookingsFromFirestore();
                    }

                    @Override
                    public void onFailure(String error) {
                        android.widget.Toast.makeText(admin_dashboard.this,
                                "Failed to reject: " + error,
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


    //handle manage menu button
    private void manageMenueButtonHandler(){
        Button manageMenuButton = findViewById(R.id.manage_menu_button);
        manageMenuButton.setOnClickListener(v -> {
            // navigate to the admin menu management page
            android.content.Intent intent = new android.content.Intent(this, admin_manage_menu.class);
            startActivity(intent);
        });
    }
}
