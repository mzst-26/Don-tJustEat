package com.example.dontjusteat;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class my_bookings extends BaseActivity {

    private LinearLayout bookingsContainer;
    private LinearLayout latestBookingContainer;

    TextView pastBookingTitle;

    // New fields to manage review dialog and image picking
    private Dialog reviewDialog;
    private int selectedRating = 0;
    private Uri selectedImageUri = null;
    private static final int PICK_IMAGE_REQUEST = 1001;

    // Simple data holder for the cards
    private static class BookingCard {
        String location;
        String date;
        String time;
        String numberOfGuests;
        String booking_status;
        String reference_number;

        String location_address;
        int bigImageResId;
        int smallImageResId;

        BookingCard(String location, String date,String time, String numberOfGuests, String booking_status, String reference_number, String location_address, int bigImageResId, int smallImageResId) {
            this.location = location;
            this.date = date;
            this.time = time;
            this.numberOfGuests = numberOfGuests;
            this.booking_status = booking_status;
            this.reference_number = reference_number;
            this.location_address = location_address;
            this.bigImageResId = bigImageResId;
            this.smallImageResId = smallImageResId;
        }
        List<BookingCard> data;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireCustomerOrFinish()) {
            return;
        }
        setContentView(R.layout.my_bookings);

        Modules.applyWindowInsets(this, R.id.rootView);
        Modules.handleMenuNavigation(this);

        // Initialize the container
        bookingsContainer = findViewById(R.id.past_booking_card_container);
        latestBookingContainer = findViewById(R.id.latest_booking_container);
        pastBookingTitle = findViewById(R.id.past_booking_title);


        // Get the data
        // Fake data for now than later I will replace it with the real data from the DB
        List<BookingCard> cards = new ArrayList<>();

        cards.add(new BookingCard(
                "Bristol - Soho",
                "08/05/2025",
                "19:30",
                "2 Guests",
                "Pending",
                "#12345",
                "14 Testinghom Ave, Plymouth PL1 1PL",
                R.drawable.restaurant_image,
                R.drawable.restaurant_image
        ));

        cards.add(new BookingCard(
                "London - Center",
                "05/05/2025",
                "18:00",
                "4 Guests",
                "Completed",
                "#12347",
                "15 Testinghom Ave, Plymouth PL1 1PL",
                R.drawable.restaurant_image,
                R.drawable.restaurant_image
        ));

        cards.add(new BookingCard(
                "Plymouth - Barbican",
                "02/05/2025",
                "20:15",
                "3 Guests",
                "Cancelled",
                "#12349",
                "16 Testinghom Ave, Plymouth PL1 1PL",
                R.drawable.restaurant_image,
                R.drawable.restaurant_image
        ));


        if (cards.isEmpty()) {

            // Empty state
            cards.add(new BookingCard(
                    "Your bookings will appear here!",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    0,
                    0
            ));
            renderCards(cards, true);

        } else {

            // Always render most recent booking
            renderMostRecentCard(cards.subList(0, 1));

            // Render past bookings only if more exist
            if (cards.size() > 1) {
                pastBookingTitle.setVisibility(View.VISIBLE);
                renderCards(cards.subList(1, cards.size()), false);
            } else {
                bookingsContainer.removeAllViews();
            }
        }









    }

    // Render the cards in the container
    private void renderCards(List<BookingCard> cards, Boolean isListEmpty) {
        LayoutInflater inflater = LayoutInflater.from(this);
        bookingsContainer.removeAllViews(); // clear old ones if any

        for (BookingCard cardData : cards) {
            //Inflate the card layout
            View cardView = inflater.inflate(R.layout.component_item_past_booking_card, bookingsContainer, false);

            //get the references to views inside the card
            ImageView imgSmall = cardView.findViewById(R.id.img_small);
            ImageView imgBig = cardView.findViewById(R.id.img_big);
            TextView txtLocation = cardView.findViewById(R.id.text_location);
            TextView txtTime = cardView.findViewById(R.id.text_date);

            //set the data to the views
            imgSmall.setImageResource(cardData.smallImageResId);
            imgBig.setImageResource(cardData.bigImageResId);
            txtLocation.setText(cardData.location);
            txtTime.setText(cardData.date);
            if (!isListEmpty) {
                cardView.setOnClickListener(v -> popUpHandler(cardData));
            }


            // add this card to the container to be displayed
            bookingsContainer.addView(cardView);
        }
    }

    private void renderMostRecentCard(List<BookingCard> cards) {
        LayoutInflater inflater = LayoutInflater.from(this);
        latestBookingContainer.removeAllViews(); // clear old ones if any

        for (BookingCard cardData : cards) {
            //Inflate the card layout
            View cardView = inflater.inflate(R.layout.component_my_booking_latest_booking, latestBookingContainer, false);

            //get the references to views inside the card
            TextView txtReference = cardView.findViewById(R.id.latest_booking_reference_number);
            ImageView imgLocation = cardView.findViewById(R.id.latest_booking_location_image);
            TextView textLocationName = cardView.findViewById(R.id.latest_booking_location_name);
            TextView textTime = cardView.findViewById(R.id.latest_booking_time);
            TextView textDate = cardView.findViewById(R.id.latest_booking_date);
            TextView textGuests = cardView.findViewById(R.id.latest_booking_guest_number);
            TextView locationAddress = cardView.findViewById(R.id.latest_location_address);



            //set the data to the views
            txtReference.setText(cardData.reference_number);
            imgLocation.setImageResource(cardData.bigImageResId);
            textLocationName.setText(cardData.location);
            textTime.setText(cardData.time);
            textDate.setText(cardData.date);
            textGuests.setText(cardData.numberOfGuests);
            locationAddress.setText(cardData.location_address);

            cardView.setOnClickListener(v -> popUpHandler(cardData));

            // add this card to the container to be displayed
            latestBookingContainer.addView(cardView);
        }
    }

    private void popUpHandler(BookingCard booking) {

        Dialog popUp = new Dialog(this);
        popUp.setContentView(R.layout.component_my_booking_detail_popup);
        popUp.setCancelable(true);

        // Make background transparent (important for rounded popup)
        if (popUp.getWindow() != null) {
            popUp.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // get all the views
        TextView textReference = popUp.findViewById(R.id.popUp_booking_reference_number);
        TextView location = popUp.findViewById(R.id.popUp_booking_location);
        TextView time = popUp.findViewById(R.id.popUp_booking_time);
        TextView date = popUp.findViewById(R.id.popUp_booking_date);
        TextView guests = popUp.findViewById(R.id.popUp_booking_guest_number);
        TextView status = popUp.findViewById(R.id.popUp_booking_status);
        TextView locationAddress = popUp.findViewById(R.id.popUp_booking_location_address);


        //get all the buttons
        Button statusBtn = popUp.findViewById(R.id.StatusUpdateButton);
        Button reviewBtn = popUp.findViewById(R.id.LeaveAReviewButton);
        Button editBtn = popUp.findViewById(R.id.RequestEditButton);
        Button cancelBtn = popUp.findViewById(R.id.RequestCancelButton);

        // fill the views with data
        textReference.setText(booking.reference_number);
        location.setText(booking.location);
        time.setText(booking.time); // placeholder
        date.setText(booking.date);
        guests.setText(booking.numberOfGuests); // placeholder
        status.setText(booking.booking_status);
        locationAddress.setText(booking.location_address);

        // handle the buttons
        statusBtn.setOnClickListener(v -> bookingStatusUpdateHandler());
        reviewBtn.setOnClickListener(v -> leaveAReviewHandler());
        editBtn.setOnClickListener(v -> requestAnEditHandler());
        cancelBtn.setOnClickListener(v -> requestCancellationHandler());


        // show the popup
        popUp.show();
    }


    private void bookingStatusUpdateHandler() {
        Intent intent = new Intent(this, customer_my_notifications.class);
        startActivity(intent);

    }

    private void leaveAReviewHandler() {
        // open the review dialog and wire interactions
        reviewDialog = new Dialog(this);
        reviewDialog.setContentView(R.layout.component_customer_leave_review);
        reviewDialog.setCancelable(true);

        if (reviewDialog.getWindow() != null) {
            reviewDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // stars
        ImageView iv1 = reviewDialog.findViewById(R.id.iv_star_1);
        ImageView iv2 = reviewDialog.findViewById(R.id.iv_star_2);
        ImageView iv3 = reviewDialog.findViewById(R.id.iv_star_3);
        ImageView iv4 = reviewDialog.findViewById(R.id.iv_star_4);
        ImageView iv5 = reviewDialog.findViewById(R.id.iv_star_5);

        View.OnClickListener starClick = v -> {
            if (v.getId() == R.id.iv_star_1) selectedRating = 1;
            else if (v.getId() == R.id.iv_star_2) selectedRating = 2;
            else if (v.getId() == R.id.iv_star_3) selectedRating = 3;
            else if (v.getId() == R.id.iv_star_4) selectedRating = 4;
            else if (v.getId() == R.id.iv_star_5) selectedRating = 5;
            updateStars(selectedRating);
        };

        iv1.setOnClickListener(starClick);
        iv2.setOnClickListener(starClick);
        iv3.setOnClickListener(starClick);
        iv4.setOnClickListener(starClick);
        iv5.setOnClickListener(starClick);

        //upload photo area
        LinearLayout uploadArea = reviewDialog.findViewById(R.id.ll_upload_photo);
        final TextView tvUploadText = reviewDialog.findViewById(R.id.tv_upload_text);
        final ImageView ivPhotoPreview = reviewDialog.findViewById(R.id.iv_photo_preview);

        uploadArea.setOnClickListener(v -> {
            // launch image picker
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // submit button
        Button submitBtn = reviewDialog.findViewById(R.id.btn_submit_review);
        final EditText etDescription = reviewDialog.findViewById(R.id.et_review_description);
        submitBtn.setOnClickListener(v -> {
            String description = etDescription.getText() != null ? etDescription.getText().toString() : "";
            // For now, I just show a Toast with collected info and dismiss.
            String msg = "Submitted review — Rating: " + selectedRating + " Description length: " + description.length();
            if (selectedImageUri != null) msg += " Photo: yes";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

            // reset selection
            selectedRating = 0;
            selectedImageUri = null;
            reviewDialog.dismiss();
        });

        //If image was previously selected and dialog is re-used then show preview
        if (selectedImageUri != null) {
            ivPhotoPreview.setVisibility(View.VISIBLE);
            ivPhotoPreview.setImageURI(selectedImageUri);
            tvUploadText.setVisibility(View.GONE);
        }

        reviewDialog.show();
    }

    private void updateStars(int rating) {
        if (reviewDialog == null) return;
        ImageView iv1 = reviewDialog.findViewById(R.id.iv_star_1);
        ImageView iv2 = reviewDialog.findViewById(R.id.iv_star_2);
        ImageView iv3 = reviewDialog.findViewById(R.id.iv_star_3);
        ImageView iv4 = reviewDialog.findViewById(R.id.iv_star_4);
        ImageView iv5 = reviewDialog.findViewById(R.id.iv_star_5);

        // use android star drawables
        iv1.setImageResource(rating >= 1 ? R.drawable.star_solid_full : R.drawable.star_regular_empty);
        iv2.setImageResource(rating >= 2 ? R.drawable.star_solid_full : R.drawable.star_regular_empty);
        iv3.setImageResource(rating >= 3 ? R.drawable.star_solid_full : R.drawable.star_regular_empty);
        iv4.setImageResource(rating >= 4 ? R.drawable.star_solid_full : R.drawable.star_regular_empty);
        iv5.setImageResource(rating >= 5 ? R.drawable.star_solid_full : R.drawable.star_regular_empty);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (reviewDialog != null && reviewDialog.isShowing()) {
                ImageView ivPhotoPreview = reviewDialog.findViewById(R.id.iv_photo_preview);
                TextView tvUploadText = reviewDialog.findViewById(R.id.tv_upload_text);
                if (ivPhotoPreview != null) {
                    ivPhotoPreview.setVisibility(View.VISIBLE);
                    ivPhotoPreview.setImageURI(selectedImageUri);
                }
                if (tvUploadText != null) {
                    tvUploadText.setVisibility(View.GONE);
                }
            }
        }
    }

    private void requestAnEditHandler() {
        // open dialog for requesting an edit (frontend-only)
        Dialog editDialog = new Dialog(this);
        editDialog.setContentView(R.layout.component_customer_request_edit_booking);
        editDialog.setCancelable(true);

        if (editDialog.getWindow() != null) {
            editDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        DatePicker dp = editDialog.findViewById(R.id.dp_edit_date);
        Slider timeSlider = editDialog.findViewById(R.id.slider_edit_time);
        TextView timeTitle = editDialog.findViewById(R.id.tv_edit_time_display);
        Slider guestsSlider = editDialog.findViewById(R.id.slider_edit_guests);
        TextView tvGuests = editDialog.findViewById(R.id.tv_edit_guests_display);
        Button submitButton = editDialog.findViewById(R.id.btn_edit_submit);
        Button cancelButton = editDialog.findViewById(R.id.btn_edit_cancel);

        // initialize DatePicker to today and set minimum date to today
        Calendar cal = Calendar.getInstance();
        dp.setMinDate(cal.getTimeInMillis());
        dp.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

        // time mapping: slider values -> time strings (example slots)
        final String[] timeSlots = new String[] { "17:00", "18:00", "19:00", "20:00", "21:00" };
        // set initial display according to slider default
        int initialTimeIndex = Math.min(timeSlots.length - 1, Math.max(0, (int) timeSlider.getValue()));
        timeTitle.setText(timeSlots[initialTimeIndex]);

        // Add label formatter to show time on the thumb
        timeSlider.setLabelFormatter(value -> {
            int idx = Math.min(timeSlots.length - 1, Math.max(0, (int) value));
            return timeSlots[idx];
        });

        timeSlider.addOnChangeListener((slider, value, fromUser) -> {
            int idx = Math.min(timeSlots.length - 1, Math.max(0, (int) value));
            timeTitle.setText(timeSlots[idx]);
        });

        // guests slider
        tvGuests.setText(String.valueOf((int) guestsSlider.getValue()));

        // Add label formatter to show "Table for X" on the thumb
        guestsSlider.setLabelFormatter(value -> {
            return "Table for " + (int) value;
        });

        guestsSlider.addOnChangeListener((slider, value, fromUser) -> {
            tvGuests.setText(String.valueOf((int) value));
        });

        cancelButton.setOnClickListener(v -> editDialog.dismiss());

        submitButton.setOnClickListener(v -> {
            int day = dp.getDayOfMonth();
            int month = dp.getMonth() + 1; // month is 0-based
            int year = dp.getYear();
            String selectedDate = String.format("%04d-%02d-%02d", year, month, day);

            int timeIdx = Math.min(timeSlots.length - 1, (int) timeSlider.getValue());
            String selectedTime = timeSlots[timeIdx];

            int selectedGuests = (int) guestsSlider.getValue();

            String msg = "Requested edit — Date: " + selectedDate + " Time: " + selectedTime + " Guests: " + selectedGuests;
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

            // frontend-only: dismiss after showing confirmation
            editDialog.dismiss();
        });

        editDialog.show();
    }

    private void requestCancellationHandler() {


    }

}
