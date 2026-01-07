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

import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.dontjusteat.repositories.BookingEditRepository;
import com.example.dontjusteat.repositories.BookingDataRepository;
import com.example.dontjusteat.repositories.BookingCancelRepository;
import com.example.dontjusteat.repositories.ReviewRepository;
import com.google.firebase.Timestamp;

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

        long sortTimestamp;

        BookingCard(String location, String date, String time, String numberOfGuests, String booking_status, String reference_number, String location_address, int bigImageResId, int smallImageResId, long sortTimestamp) {
            this.location = location;
            this.date = date;
            this.time = time;
            this.numberOfGuests = numberOfGuests;
            this.booking_status = booking_status;
            this.reference_number = reference_number;
            this.location_address = location_address;
            this.bigImageResId = bigImageResId;
            this.smallImageResId = smallImageResId;
            this.sortTimestamp = sortTimestamp;
        }
    }



    // map bookingId to meta for edit
    private final Map<String, BookingDataRepository.BookingDisplayModel> bookingMetaById = new HashMap<>();
    private String currentBookingIdForEdit = null;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireCustomerOrFinish()) {
            return;
        }
        setContentView(R.layout.my_bookings);

        Modules.applyWindowInsets(this, R.id.rootView);
        Modules.handleMenuNavigation(this);
        Modules.handleSimpleHeaderNavigation(this);

        // initialize the containers for latest and past bookings
        bookingsContainer = findViewById(R.id.past_booking_card_container);
        latestBookingContainer = findViewById(R.id.latest_booking_container);
        pastBookingTitle = findViewById(R.id.past_booking_title);

        loadBookings();

    }

    private void loadBookings() {
        // fetch user bookings from repository
        BookingDataRepository repo = new BookingDataRepository();
        repo.fetchUserBookings(new BookingDataRepository.OnBookingsLoaded() {
            @Override
            public void onSuccess(List<BookingDataRepository.BookingDisplayModel> bookings) {
                // convert to card model and render
                renderLoadedCards(toCards(bookings));
            }

            @Override
            public void onFailure() {
                // show empty state if fetch fails
                renderEmptyState();
            }
        });
    }

    private List<BookingCard> toCards(List<BookingDataRepository.BookingDisplayModel> list) {
        // convert display models to card view models and cache meta for edit
        List<BookingCard> cards = new ArrayList<>();
        if (list == null) return cards;
        for (int i = 0; i < list.size(); i++) {
            BookingDataRepository.BookingDisplayModel m = list.get(i);
            BookingCard c = new BookingCard(
                    m.locationName, m.date, m.time, m.guests, m.status, m.bookingId,
                    m.address, R.drawable.restaurant_image, R.drawable.restaurant_image, m.sortTimestamp
            );
            cards.add(c);
            // cache for later when user taps edit
            bookingMetaById.put(m.bookingId, m);
            if (i == 0) { lastMostRecentMeta = m; }
        }
        return cards;
    }

    private void renderLoadedCards(List<BookingCard> cards) {
        // sort by newest first and split into latest and past
        if (cards.isEmpty()) {
            renderEmptyState();
            return;
        }

        Collections.sort(cards, Comparator.comparingLong(c -> -c.sortTimestamp));

        // show the most recent booking
        renderMostRecentCard(cards.subList(0, 1));
        // show past bookings if more than one exists
        if (cards.size() > 1) {
            pastBookingTitle.setVisibility(View.VISIBLE);
            renderCards(cards.subList(1, cards.size()), false);
        } else {
            bookingsContainer.removeAllViews();
            pastBookingTitle.setVisibility(View.GONE);
        }
    }

    private void renderEmptyState() {
        List<BookingCard> cards = new ArrayList<>();
        cards.add(new BookingCard(
                "Your bookings will appear here!",
                "",
                "",
                "",
                "",
                "",
                "",
                0,
                0,
                System.currentTimeMillis()
        ));
        renderCards(cards, true);
        latestBookingContainer.removeAllViews();
        pastBookingTitle.setVisibility(View.GONE);
    }

    // Render the cards in the container
    private void renderCards(List<BookingCard> cards, Boolean isListEmpty) {
        LayoutInflater inflater = LayoutInflater.from(this);
        bookingsContainer.removeAllViews();

        for (BookingCard cardData : cards) {
            // inflate card layout
            View cardView = inflater.inflate(R.layout.component_item_past_booking_card, bookingsContainer, false);

            // get view references
            ImageView imgSmall = cardView.findViewById(R.id.img_small);
            ImageView imgBig = cardView.findViewById(R.id.img_big);
            TextView txtLocation = cardView.findViewById(R.id.text_location);
            TextView txtTime = cardView.findViewById(R.id.text_date);

            // set data and click listener
            imgSmall.setImageResource(cardData.smallImageResId);
            imgBig.setImageResource(cardData.bigImageResId);
            txtLocation.setText(cardData.location != null ? cardData.location : "");
            txtTime.setText(cardData.date + (cardData.time.isEmpty() ? "" : ("  " + cardData.time)));
            if (!isListEmpty) {
                cardView.setOnClickListener(v -> popUpHandler(cardData));
            }

            bookingsContainer.addView(cardView);
        }
    }

    // keep a reference of the latest booking meta to pass to edit repo
    private BookingCard lastMostRecentCard;
    private BookingDataRepository.BookingDisplayModel lastMostRecentMeta;

    private void renderMostRecentCard(List<BookingCard> cards) {
        LayoutInflater inflater = LayoutInflater.from(this);
        latestBookingContainer.removeAllViews();
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
            // cache last displayed most recent card
            lastMostRecentCard = cardData;
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

        // disable edit and cancel buttons for certain statuses
        boolean shouldDisable = "CANCELED".equals(booking.booking_status) ||
                                "CHANGE REQUEST".equals(booking.booking_status) ||
                                "REJECTED BY STAFF".equals(booking.booking_status);

        if (shouldDisable) {
            // disable edit button
            editBtn.setEnabled(false);
            editBtn.setAlpha(0.5f);

            // disable cancel button
            cancelBtn.setEnabled(false);
            cancelBtn.setAlpha(0.5f);
        } else {
            editBtn.setOnClickListener(v -> requestAnEditHandler());
            cancelBtn.setOnClickListener(v -> requestCancellationHandler());
        }


        // set selected bookingId for edit
        currentBookingIdForEdit = booking.reference_number;

        // show the popup
        popUp.show();
    }


    private void bookingStatusUpdateHandler() {
        Intent intent = new Intent(this, customer_my_notifications.class);
        startActivity(intent);

    }

    private void leaveAReviewHandler() {
        // check if already reviewed this booking
        BookingDataRepository.BookingDisplayModel meta = bookingMetaById.get(currentBookingIdForEdit);
        if (meta == null) {
            Toast.makeText(this, "Missing booking data", Toast.LENGTH_SHORT).show();
            return;
        }

        ReviewRepository reviewRepo = new ReviewRepository();
        reviewRepo.checkIfReviewed(meta.restaurantId, meta.bookingId, alreadyReviewed -> {
            if (alreadyReviewed) {
                Toast.makeText(this, "You already reviewed this booking", Toast.LENGTH_SHORT).show();
                return;
            }

            // open review dialog
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

            // submit button
            Button submitBtn = reviewDialog.findViewById(R.id.btn_submit_review);
            final EditText etDescription = reviewDialog.findViewById(R.id.et_review_description);
            submitBtn.setOnClickListener(v -> {
                if (selectedRating == 0) {
                    Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                    return;
                }

                String description = etDescription.getText() != null ? etDescription.getText().toString() : "";

                // submit to firestore
                reviewRepo.submitReview(meta.restaurantId, meta.bookingId, selectedRating, description,
                        new ReviewRepository.OnReviewSubmitListener() {
                            @Override public void onSuccess() {
                                Toast.makeText(my_bookings.this, "Review submitted", Toast.LENGTH_SHORT).show();
                                selectedRating = 0;
                                selectedImageUri = null;
                                reviewDialog.dismiss();
                            }
                            @Override public void onFailure(String error) {
                                Toast.makeText(my_bookings.this, error != null ? error : "Failed", Toast.LENGTH_LONG).show();
                            }
                        });
            });

            reviewDialog.show();
        });
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

        final String[] timeSlots = new String[] { "17:00", "18:00", "19:00", "20:00", "21:00" };
        int initialTimeIndex = Math.min(timeSlots.length - 1, Math.max(0, (int) timeSlider.getValue()));
        timeTitle.setText(timeSlots[initialTimeIndex]);

        timeSlider.setLabelFormatter(value -> {
            int idx = Math.min(timeSlots.length - 1, Math.max(0, (int) value));
            return timeSlots[idx];
        });
        timeSlider.addOnChangeListener((slider, value, fromUser) -> {
            int idx = Math.min(timeSlots.length - 1, Math.max(0, (int) value));
            timeTitle.setText(timeSlots[idx]);
        });

        tvGuests.setText(String.valueOf((int) guestsSlider.getValue()));
        guestsSlider.setLabelFormatter(value -> "Table for " + (int) value);
        guestsSlider.addOnChangeListener((slider, value, fromUser) -> tvGuests.setText(String.valueOf((int) value)));

        cancelButton.setOnClickListener(v -> editDialog.dismiss());

        submitButton.setOnClickListener(v -> {
            // build new start time from date picker + slider slot text
            int y = dp.getYear();
            int m = dp.getMonth();
            int d = dp.getDayOfMonth();
            String hhmm = timeTitle.getText() != null ? timeTitle.getText().toString() : "17:00";
            try {
                String[] parts = hhmm.split(":");
                int hh = Integer.parseInt(parts[0]);
                int mm = Integer.parseInt(parts[1]);
                Calendar chosen = Calendar.getInstance();
                chosen.set(Calendar.YEAR, y);
                chosen.set(Calendar.MONTH, m);
                chosen.set(Calendar.DAY_OF_MONTH, d);
                chosen.set(Calendar.HOUR_OF_DAY, hh);
                chosen.set(Calendar.MINUTE, mm);
                chosen.set(Calendar.SECOND, 0);
                chosen.set(Calendar.MILLISECOND, 0);
                Timestamp newStart = new Timestamp(chosen.getTime());

                if (currentBookingIdForEdit == null) { Toast.makeText(this, "No booking selected", Toast.LENGTH_SHORT).show(); return; }
                BookingDataRepository.BookingDisplayModel meta = bookingMetaById.get(currentBookingIdForEdit);
                if (meta == null) { Toast.makeText(this, "Missing booking data", Toast.LENGTH_SHORT).show(); return; }

                int newGuests = (int) guestsSlider.getValue();

                BookingEditRepository repo = new BookingEditRepository();
                repo.requestEdit(meta.restaurantId, meta.bookingId, meta.tableId, meta.startTime, meta.durationMinutes, newStart, newGuests,
                        new BookingEditRepository.OnRequestEditListener() {
                            @Override public void onSuccess() {
                                Toast.makeText(my_bookings.this, "Change requested", Toast.LENGTH_SHORT).show();
                                editDialog.dismiss();
                                loadBookings();
                            }
                            @Override public void onFailure(String error) {
                                Toast.makeText(my_bookings.this, error != null ? error : "Failed", Toast.LENGTH_LONG).show();
                            }
                        });
            } catch (Exception ex) {
                Toast.makeText(this, "Invalid time", Toast.LENGTH_SHORT).show();
            }
        });

        editDialog.show();
    }

    private void requestCancellationHandler() {
        // get meta for current booking
        if (currentBookingIdForEdit == null) {
            Toast.makeText(this, "No booking selected", Toast.LENGTH_SHORT).show();
            return;
        }
        BookingDataRepository.BookingDisplayModel meta = bookingMetaById.get(currentBookingIdForEdit);
        if (meta == null) {
            Toast.makeText(this, "Missing booking data", Toast.LENGTH_SHORT).show();
            return;
        }

        // confirm cancellation
        new android.app.AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // compute end time from start + duration
                    long endMs = meta.startTime.toDate().getTime() + (meta.durationMinutes * 60 * 1000L);
                    Timestamp endTime = new Timestamp(endMs/1000, (int)((endMs%1000)*1000000));

                    // call repo to cancel and release locks
                    BookingCancelRepository repo = new BookingCancelRepository();
                    repo.cancelBooking(meta.restaurantId, meta.bookingId, meta.tableId, meta.startTime, endTime,
                            new BookingCancelRepository.OnCancelListener() {
                                @Override public void onSuccess() {
                                    Toast.makeText(my_bookings.this, "Booking canceled", Toast.LENGTH_SHORT).show();
                                    loadBookings();
                                }
                                @Override public void onFailure(String error) {
                                    Toast.makeText(my_bookings.this, error != null ? error : "Cancellation failed", Toast.LENGTH_LONG).show();
                                }
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }




}
