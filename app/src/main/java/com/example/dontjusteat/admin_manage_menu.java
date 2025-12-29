package com.example.dontjusteat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class admin_manage_menu extends AppCompatActivity {

    // Data model for menu items (Firebase-ready structure)
    static class MenuItem {
        String itemId;
        String title;
        String description;
        double price;
        int imageResource; // Later replace with String imageUrl for Firebase

        MenuItem(String itemId, String title, String description, double price, int imageResource) {
            this.itemId = itemId;
            this.title = title;
            this.description = description;
            this.price = price;
            this.imageResource = imageResource;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_menu);

        //import modules
        Modules.applyWindowInsets(this, R.id.rootView);
        admin_modules.handleMenuNavigation(this);
        Modules.handleSimpleHeaderNavigation(this);

        // Load and display menu items
        List<MenuItem> menuItems = getFakeMenuData();
        displayMenuItems(menuItems);
    }

    // Generate fake menu data (replace with Firebase fetch later)
    private List<MenuItem> getFakeMenuData() {
        List<MenuItem> items = new ArrayList<>();

        items.add(new MenuItem("ITEM001", "Margherita Pizza", "Classic pizza with tomato, mozzarella, and basil", 12.99, R.drawable.restaurant_image));
        items.add(new MenuItem("ITEM002", "Caesar Salad", "Fresh romaine lettuce with parmesan and croutons", 8.50, R.drawable.restaurant_image));
        items.add(new MenuItem("ITEM003", "Beef Burger", "Grilled beef patty with lettuce, tomato, and cheese", 14.99, R.drawable.restaurant_image));
        items.add(new MenuItem("ITEM004", "Pasta Carbonara", "Creamy pasta with bacon and parmesan cheese", 13.50, R.drawable.restaurant_image));
        items.add(new MenuItem("ITEM005", "Fish & Chips", "Battered cod with crispy fries and tartar sauce", 15.99, R.drawable.restaurant_image));
        items.add(new MenuItem("ITEM006", "Chicken Wings", "Spicy buffalo wings with ranch dressing", 10.99, R.drawable.restaurant_image));
        items.add(new MenuItem("ITEM007", "Veggie Wrap", "Grilled vegetables wrapped in a tortilla", 9.99, R.drawable.restaurant_image));
        items.add(new MenuItem("ITEM008", "Steak Frites", "Grilled ribeye steak with french fries", 24.99, R.drawable.restaurant_image));
        items.add(new MenuItem("ITEM009", "Tomato Soup", "Homemade tomato soup with fresh basil", 6.50, R.drawable.restaurant_image));
        items.add(new MenuItem("ITEM010", "Steak with nothing", "Grilled ribeye steak with nothing souse", 140.99, R.drawable.restaurant_image));
        items.add(new MenuItem("ITEM011", "Tomato Soup but different", "Homemade tomato soup with old basil", 20.50, R.drawable.restaurant_image));

        return items;
    }

    // Display menu items in the GridLayout
    private void displayMenuItems(List<MenuItem> menuItems) {
        GridLayout container = findViewById(R.id.menu_items_container);
        if (container == null) {
            android.util.Log.e("admin_manage_menu", "GridLayout container not found!");
            return;
        }

        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);



        try {
            for (MenuItem item : menuItems) {
                //inflate the menu item card
                View card = inflater.inflate(R.layout.admin_component_menu_item_card, container, false);

                //set GridLayout.LayoutParams for proper 3-column layout
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.width = 0;
                layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
                layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
                layoutParams.setMargins(8, 8, 8, 8);
                card.setLayoutParams(layoutParams);


                // Find views in the card (only image and title now)
                ImageView image = card.findViewById(R.id.menu_item_image);
                TextView title = card.findViewById(R.id.menu_item_title);



                // bind data to views
                if (image != null) image.setImageResource(item.imageResource);
                if (title != null) title.setText(item.title);


                // Set click listener for editing/deleting
                card.setOnClickListener(v -> handleMenuItemClick(item));



                //  Add card to grid
                container.addView(card);
            }
        } catch (Exception e) {
            android.util.Log.e("admin_manage_menu", "Error displaying menu items: " + e.getMessage());
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Error loading menu items", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    // handle menu item click for editing/deleting
    private void handleMenuItemClick(MenuItem item) {

    }
}
