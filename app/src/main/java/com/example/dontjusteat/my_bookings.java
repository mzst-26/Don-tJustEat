package com.example.dontjusteat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class my_bookings extends AppCompatActivity {

    private LinearLayout bookingsContainer;

    // Simple data holder for the cards
    private static class BookingCard {
        String location;
        String time;
        int bigImageResId;
        int smallImageResId;

        BookingCard(String location, String time, int bigImageResId, int smallImageResId) {
            this.location = location;
            this.time = time;
            this.bigImageResId = bigImageResId;
            this.smallImageResId = smallImageResId;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_bookings);

        Modules.applyWindowInsets(this, R.id.rootView);
        Modules.handleMenuNavigation(this);

        // Initialize the container
        bookingsContainer = findViewById(R.id.past_booking_card_container);
        // Fake data for now than later I will replace it with the real data from the DB
        List<BookingCard> cards = new ArrayList<>();
        cards.add(new BookingCard("Bristol - Soho", "08/05", R.drawable.restaurant_image, R.drawable.restaurant_image));
        cards.add(new BookingCard("London - Center", "05/04", R.drawable.restaurant_image, R.drawable.restaurant_image));
        cards.add(new BookingCard("Plymouth - Barbican", "02/04", R.drawable.restaurant_image, R.drawable.restaurant_image));

        renderCards(cards);


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
            txtTime.setText(cardData.time);

            // add this card to the container to be displayed
            bookingsContainer.addView(cardView);
        }
    }


}
