package com.example.dontjusteat;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dontjusteat.models.Restaurant;
import com.example.dontjusteat.repositories.RestaurantRepository;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.List;

public class customer_location_detail extends BaseActivity {

    private String restaurantId;
    private int availableSlots = 0;
    private int partySize = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!requireCustomerOrFinish()) {
            return;
        }
        setContentView(R.layout.customer_location_detail);

        // get intent data
        restaurantId = getIntent().getStringExtra("restaurantId");
        availableSlots = getIntent().getIntExtra("availableSlots", 0);
        partySize = getIntent().getIntExtra("partySize", 2);

        // fetch restaurant details
        if (restaurantId != null) {
            fetchRestaurantDetails();
        }

        //Back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Get bottom sheet and its behavior
        final LinearLayout bottomSheet = findViewById(R.id.list);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Prevent full expansion beyond control
        bottomSheetBehavior.setFitToContents(false);

        // Disable dragging initially
        bottomSheetBehavior.setDraggable(true);

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
                return false;
            }
        });

        // Dynamically set peek height and expanded offset after layout
        final View header = findViewById(R.id.header_container);
        final View Upper_container = findViewById(R.id.Upper_container);
        bottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                bottomSheet.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int parentHeight = ((View) bottomSheet.getParent()).getHeight();
                int mapBottom = Upper_container.getBottom();
                int peekHeight = parentHeight - mapBottom;
                bottomSheetBehavior.setPeekHeight(peekHeight + dpToPx(28));

                int headerHeight = header.getHeight();
                bottomSheetBehavior.setExpandedOffset(headerHeight);

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        Button bookTableButton = findViewById(R.id.book_table_button);
        bookTableButton.setOnClickListener(view -> {
            Intent intent = new Intent(customer_location_detail.this, booking_summary.class);
            intent.putExtra("restaurantId", restaurantId);
            intent.putExtra("partySize", partySize);
            long requestedAfterMsExtra = getIntent().getLongExtra("requestedAfterMs", -1);
            if (requestedAfterMsExtra > 0) intent.putExtra("requestedAfterMs", requestedAfterMsExtra);
            startActivity(intent);
        });

        // Apply window insets
        Modules.applyWindowInsets(this, R.id.rootView);

        // Handle menu navigation
        Modules.handleMenuNavigation(this);

    }

    // fetch restaurant details from database
    private void fetchRestaurantDetails() {
        RestaurantRepository repo = new RestaurantRepository();
        repo.getRestaurantById(restaurantId, new RestaurantRepository.OnRestaurantFetchListener() {
            @Override
            public void onSuccess(Restaurant restaurant) {
                displayRestaurantDetails(restaurant);
                // fetch total tables count
                fetchTotalTablesCount();
                // fetch menu items after restaurant details load
                fetchMenuItems();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(customer_location_detail.this, "Failed to load restaurant details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // fetch total number of tables for the restaurant
    private void fetchTotalTablesCount() {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()

                .collection("restaurants")
                .document(restaurantId)
                .collection("tables")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int totalTables = snapshot.size();
                    // update tables available count
                    TextView tablesAvailableCount = findViewById(R.id.tables_available_count);

                    if (tablesAvailableCount != null) {
                        tablesAvailableCount.setText(String.valueOf(totalTables));
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("LOCATION_DEBUG", "Failed to load tables count: " + e.getMessage());
                });
    }

    // fetch menu items for the restaurant
    private void fetchMenuItems() {
        RestaurantRepository repo = new RestaurantRepository();
        repo.getMenuItemsByRestaurantId(restaurantId, new RestaurantRepository.OnMenuItemsListener() {
            @Override
            public void onSuccess(List<com.example.dontjusteat.models.MenuItem> items) {
                displayMenuItems(items);
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("LOCATION_DEBUG", "Failed to load menu: " + error);
            }
        });
    }

    // display menu items by inflating cards
    private void displayMenuItems(List<com.example.dontjusteat.models.MenuItem> items) {
        LinearLayout cardContainer = findViewById(R.id.card_container);
        if (cardContainer == null) return;

        cardContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        for (com.example.dontjusteat.models.MenuItem item : items) {
            View card = inflater.inflate(R.layout.component_location_detail_card, cardContainer, false);

            // set menu item data
            ImageView itemImage = card.findViewById(R.id.menu_item_image);
            TextView itemName = card.findViewById(R.id.menu_item_name);
            TextView itemDescription = card.findViewById(R.id.menu_item_description);
            TextView itemPrice = card.findViewById(R.id.menu_item_price);

            itemName.setText(item.getItemName());
            itemDescription.setText(item.getItemDes());
            itemPrice.setText(String.format("£%.2f", item.getPrice()));

            if (item.getImageURL() != null && !item.getImageURL().isEmpty()) {
                Glide.with(this).load(item.getImageURL()).into(itemImage);
            }

            cardContainer.addView(card);
        }
    }

    // fetch restaurant details from database

    // display the fetched restaurant data on the UI
    private void displayRestaurantDetails(Restaurant restaurant) {
        // set header title
        TextView headerTitle = findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(restaurant.getName());
        }

        // set location image
        ImageView restaurantImage = findViewById(R.id.restaurant_image);
        if (restaurantImage != null && restaurant.getImageUrl() != null) {
            Glide.with(this).load(restaurant.getImageUrl()).into(restaurantImage);
        }

        // set location name
        TextView locationNameText = findViewById(R.id.location_name_text);
        if (locationNameText != null) {
            locationNameText.setText(restaurant.getName());
        }

        // set address
        TextView addressText = findViewById(R.id.location_address_text);
        if (addressText != null) {
            addressText.setText(restaurant.getAddress());
        }

        android.util.Log.d("LOCATION_DEBUG", "Loaded: " + restaurant.getName() + " | Phone: " + restaurant.getPhone());
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
