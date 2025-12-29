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
    }

    // Data model for a booking
    static class Booking {
        String name;
        String tableLabel;
        String time;
        String status;
        int avatarResId;

        Booking(String name, String tableLabel, String time, String status, int avatarResId) {
            this.name = name;
            this.tableLabel = tableLabel;
            this.time = time;
            this.status = status;
            this.avatarResId = avatarResId;
        }
    }

    private List<Booking> buildSampleBookings() {
        List<Booking> list = new ArrayList<>();
        list.add(new Booking("Alice Johnson", "Table 3", "12:15 PM", "Arrived", R.drawable.logo));
        list.add(new Booking("Brian Lee", "Table 7", "12:45 PM", "Seated", R.drawable.logo));
        list.add(new Booking("Carmen Diaz", "Table 2", "1:00 PM", "New Request", R.drawable.logo));
        return list;
    }

    //inflate cards into the scroll content container based on data
    private void populateBookings(List<Booking> bookings) {

        LinearLayout container = findViewById(R.id.todays_bookings_container);

        if (container == null) return; // in cas if layout is not found
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Booking b : bookings) {
            View card = inflater.inflate(R.layout.admin_component_dashboard_todays_booking_card, container, false);

            ImageView avatar = card.findViewById(R.id.booking_avatar);
            TextView name = card.findViewById(R.id.booking_name);
            TextView tableTime = card.findViewById(R.id.booking_table_time);
            TextView status = card.findViewById(R.id.booking_status);

            // Bind data
            avatar.setImageResource(b.avatarResId);
            name.setText(b.name);
            tableTime.setText(b.tableLabel + " • " + b.time);
            status.setText(b.status);


            container.addView(card);
        }
    }
}
