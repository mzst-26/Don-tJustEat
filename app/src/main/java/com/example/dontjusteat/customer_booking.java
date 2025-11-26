package com.example.dontjusteat;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class customer_booking extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_booking);

        // Get bottom sheet and its behavior
        final LinearLayout bottomSheet = findViewById(R.id.list);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Prevent full expansion beyond control
        bottomSheetBehavior.setFitToContents(false);

        // Disable dragging initially
        bottomSheetBehavior.setDraggable(false);

        // Enable dragging only when touching handle
        View handle = findViewById(R.id.handle_container);
        handle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        bottomSheetBehavior.setDraggable(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        bottomSheetBehavior.setDraggable(false);
                        break;
                }
                return false; // allow other events like click
            }
        });

        // Dynamically set peek height and expanded offset after layout
        final View header = findViewById(R.id.header_container);
        final View mapView = findViewById(R.id.mapView);
        bottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bottomSheet.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get parent height (screen height minus status/nav bars if any)
                int parentHeight = ((View) bottomSheet.getParent()).getHeight();

                // Minimum visible height: lip to lip with map bottom
                int mapBottom = mapView.getBottom();
                int peekHeight = parentHeight - mapBottom;
                bottomSheetBehavior.setPeekHeight(peekHeight);

                // Maximum expansion: up to below the header (covers map but keeps header visible)
                int headerHeight = header.getHeight();
                bottomSheetBehavior.setExpandedOffset(headerHeight);

                // Set initial state to collapsed (which is now lip to lip)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // Get select button
        Button selectButton = findViewById(R.id.select_button);
        selectButton.setOnClickListener(view -> {
            Intent intent = new Intent(customer_booking.this, customer_location_detail.class);
            startActivity(intent);

        });
    }

    // Helper method: convert dp to pixels
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}