package com.example.dontjusteat;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class my_bookings extends AppCompatActivity {

    private LinearLayout bookingsContainer;
    private LinearLayout latestBookingContainer;


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
        setContentView(R.layout.my_bookings);

        Modules.applyWindowInsets(this, R.id.rootView);
        Modules.handleMenuNavigation(this);

        // Initialize the container
        bookingsContainer = findViewById(R.id.past_booking_card_container);
        latestBookingContainer = findViewById(R.id.latest_booking_container);

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


            if (cards.size() > 0){
//                BookingCard fisrtCard = cards.get(0);
                int numberOfCards = cards.size();
                renderCards(cards.subList(1, numberOfCards));
            }
            renderMostRecentCard(cards.subList(0, 1));








    }

    // Render the cards in the container
    private void renderCards(List<BookingCard> cards) {
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
            cardView.setOnClickListener(v -> popUpHandler(cardData));

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

    }

    private void leaveAReviewHandler() {

    }

    private void requestAnEditHandler() {

    }
    private void requestCancellationHandler() {

    }

}
