package com.example.dontjusteat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class admin_dashboard extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);
        Modules.applyWindowInsets(this, R.id.rootView);

        ImageView back_button = findViewById(R.id.back_button);
        //remove this button for this page
        back_button.setVisibility(ImageView.GONE);

        // Build sample data and render cards
        List<Booking> todaysBookings = buildSampleBookings();
        populateBookings(todaysBookings);

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
        int avatarResId;

        Booking(String name, String tableLabel, String time, String date, int guests, String bookingId, String status, int avatarResId) {
            this.name = name;
            this.tableLabel = tableLabel;
            this.time = time;
            this.date = date;
            this.guests = guests;
            this.bookingId = bookingId;
            this.status = status;
            this.avatarResId = avatarResId;
        }
    }

    private List<Booking> buildSampleBookings() {
        List<Booking> list = new ArrayList<>();
        list.add(new Booking("Alice Johnson", "Table 3", "12:15 PM", "Dec 29, 2025", 2, "BK001", "Arrived", R.drawable.logo));
        list.add(new Booking("Brian Lee", "Table 7", "12:45 PM", "Dec 29, 2025", 4, "BK002", "Seated", R.drawable.logo));
        list.add(new Booking("Carmen Diaz", "Table 2", "1:00 PM", "Dec 29, 2025", 3, "BK003", "New Request", R.drawable.logo));
        list.add(new Booking("David Chen", "Table 4", "1:30 PM", "Dec 29, 2025", 6, "BK004", "Requested Change", R.drawable.logo));
        list.add(new Booking("Eva Müller", "Table 5", "2:00 PM", "Dec 29, 2025", 5, "BK005", "Canceled", R.drawable.logo));
        return list;
    }

    //inflate cards into the scroll content container based on data
    private void populateBookings(List<Booking> bookings) {

        LinearLayout todaysContentContainer = findViewById(R.id.todays_bookings_container);
        LinearLayout quickActionsToTakeContainer = findViewById(R.id.actions_to_take_container);

        LinearLayout parentQuickActionsToTakeContainer = findViewById(R.id.parent_actions_to_take_container);

        if (todaysContentContainer == null) return; // in case if layout is not found

        todaysContentContainer.removeAllViews();
        if (quickActionsToTakeContainer != null) {
            quickActionsToTakeContainer.removeAllViews();
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        int quickActionsCount = 0;

        for (Booking b : bookings) {
            //determine which container to use based on status
            boolean needsQuickAction = b.status.equals("New Request") ||
                                      b.status.equals("Requested Change") ||
                                      b.status.equals("Canceled");

            LinearLayout targetContainer = needsQuickAction ? quickActionsToTakeContainer : todaysContentContainer;

            if (targetContainer == null) continue;

            if (needsQuickAction) {
                quickActionsCount++;
            }

            View card = inflater.inflate(R.layout.admin_component_dashboard_todays_booking_card, targetContainer, false);

            ImageView avatar = card.findViewById(R.id.booking_avatar);
            TextView name = card.findViewById(R.id.booking_name);
            TextView tableTime = card.findViewById(R.id.booking_table_time);
            TextView status = card.findViewById(R.id.booking_status);

            // Bind data
            avatar.setImageResource(b.avatarResId);
            name.setText(b.name);
            tableTime.setText(b.tableLabel + " • " + b.time);
            status.setText(b.status);

            // Different click handlers based on container
            if (needsQuickAction) {
                //quick action cards - handle approval or the rejection
                card.setOnClickListener(v -> {
                    handleQuickActionClick(b);
                });
            } else {
                //today's booking cards - view details
                card.setOnClickListener(v -> {
                    handleBookingDetailsClick(b);
                });
            }

            targetContainer.addView(card);
        }

        // hide parent container if there are no quick actions
        if (parentQuickActionsToTakeContainer != null) {
            if (quickActionsCount == 0) {
                parentQuickActionsToTakeContainer.setVisibility(View.GONE);
            } else {
                parentQuickActionsToTakeContainer.setVisibility(View.VISIBLE);
            }
        }
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
        customerInfo.setText("Booking ID: #" + booking.bookingId);
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
        android.widget.Toast.makeText(this,
            "Accepted: " + booking.name + " - " + booking.status,
            android.widget.Toast.LENGTH_SHORT).show();

        // later that I will do: Update booking status in database
        // later that I will do: Send notification to customer
        // later that I will do: Refresh the booking lists


        // for now we just refresh the UI
        List<Booking> updatedBookings = buildSampleBookings();
        populateBookings(updatedBookings);
    }

    // Handle rejecting a booking request
    private void handleRejectBooking(Booking booking) {
        android.widget.Toast.makeText(this,
            "Rejected: " + booking.name + " - " + booking.status,
            android.widget.Toast.LENGTH_SHORT).show();

        // later that I will do: Update booking status in database
        // later that I will do: Send notification to customer
        // later that I will do: Refresh the booking lists


        //For now we just refresh the UI
        List<Booking> updatedBookings = buildSampleBookings();
        populateBookings(updatedBookings);
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
        customerInfo.setText("Booking ID: #" + booking.bookingId);
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
