package com.example.dontjusteat;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class admin_manage_menu extends AppCompatActivity {

    // Image picker launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView currentEditingImageView;
    private Uri selectedImageUri;

    static class MenuItem {
        String itemId;
        String title;
        String description;
        double price;
        int imageResource;
        String imageUrl;

        MenuItem(String itemId, String title, String description, double price, int imageResource) {
            this.itemId = itemId;
            this.title = title;
            this.description = description;
            this.price = price;
            this.imageResource = imageResource;
            this.imageUrl = null; // Will be populated when connected to databse
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_menu);

        // initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Get selected image URI
                        selectedImageUri = result.getData().getData();
                        if (currentEditingImageView != null && selectedImageUri != null) {
                            // Display selected image in the popup
                            currentEditingImageView.setImageURI(selectedImageUri);
                            Toast.makeText(this, "Image selected (not saved yet)", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

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
        Dialog dialog = createMenuItemDialog();
        bindDialogViews(dialog, item);
        dialog.show();
    }

    //create and configure the dialog (in this file I seperated the dialog configuration to make the code more clear. later I will apply this to every file)
    private Dialog createMenuItemDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.admin_component_menu_card_detail);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

    //Bind all dialg views and set up interactions
    private void bindDialogViews(Dialog dialog, MenuItem item) {
        // find all views
        ImageView itemImage = dialog.findViewById(R.id.itemImageView);
        TextView nameTv = dialog.findViewById(R.id.nameTv);
        EditText nameEt = dialog.findViewById(R.id.nameEt);
        ImageView editNameIcon = dialog.findViewById(R.id.editNameIcon);
        TextView descriptionTv = dialog.findViewById(R.id.descriptionTv);
        EditText descriptionEt = dialog.findViewById(R.id.descriptionEt);
        ImageView editDescriptionIcon = dialog.findViewById(R.id.editDescriptionIcon);
        View replaceImageButton = dialog.findViewById(R.id.replaceImageContainer);
        TextView priceTv = dialog.findViewById(R.id.priceTv);
        EditText priceEt = dialog.findViewById(R.id.priceEt);
        ImageView editPriceIcon = dialog.findViewById(R.id.editPriceIcon);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button deleteButton = dialog.findViewById(R.id.deleteButton);

        // Fill the dialog with item data
        populateDialogData(item, itemImage, nameTv, nameEt, descriptionTv, descriptionEt, priceTv, priceEt);

        // Setup image  picker reference
        currentEditingImageView = itemImage;
        selectedImageUri = null;

        // set up the edit toggles
        setupEditToggle(nameTv, nameEt, editNameIcon);
        setupEditToggle(descriptionTv, descriptionEt, editDescriptionIcon);
        setupPriceEditToggle(priceTv, priceEt, editPriceIcon, item);

        //Setup action buttons
        setupReplaceImageButton(replaceImageButton);
        setupSaveButton(saveButton, dialog, item, nameEt, descriptionEt, priceEt);
        setupDeleteButton(deleteButton, dialog, item);
    }

    // fill the dialog with current item data
    private void populateDialogData(MenuItem item, ImageView itemImage, TextView nameTv,
                                   EditText nameEt, TextView descriptionTv, EditText descriptionEt,
                                   TextView priceTv, EditText priceEt) {
        if (itemImage != null) itemImage.setImageResource(item.imageResource);
        if (nameTv != null) nameTv.setText(item.title);
        if (nameEt != null) nameEt.setText(item.title);
        if (descriptionTv != null) descriptionTv.setText(item.description);
        if (descriptionEt != null) descriptionEt.setText(item.description);
        if (priceTv != null) priceTv.setText(String.format("$%.2f", item.price));
        if (priceEt != null) priceEt.setText(String.valueOf(item.price));
    }

    // the general edit toggle for TextView/EditText pairs
    private void setupEditToggle(TextView textView, EditText editText, ImageView editIcon) {
        if (editIcon == null || textView == null || editText == null) return;

        editIcon.setOnClickListener(v -> {
            if (textView.getVisibility() == View.VISIBLE) {
                // Switch to edit mode
                textView.setVisibility(View.GONE);
                editText.setVisibility(View.VISIBLE);
                editText.requestFocus();
            } else {
                // Switch to view mode
                textView.setVisibility(View.VISIBLE);
                editText.setVisibility(View.GONE);
                textView.setText(editText.getText().toString());
            }
        });
    }

    // specialized edit toggle for price with validation
    private void setupPriceEditToggle(TextView priceTv, EditText priceEt, ImageView editIcon, MenuItem item) {
        if (editIcon == null || priceTv == null || priceEt == null) return;

        editIcon.setOnClickListener(v -> {
            if (priceTv.getVisibility() == View.VISIBLE) {
                // Switch to edit mode
                priceTv.setVisibility(View.GONE);
                priceEt.setVisibility(View.VISIBLE);
                priceEt.requestFocus();
            } else {
                // Switch to view mode with validation
                priceTv.setVisibility(View.VISIBLE);
                priceEt.setVisibility(View.GONE);

                if (validateAndSetPrice(priceEt, priceTv)) {
                    // Valid price
                } else {
                    // Invalid price, revert to original
                    priceTv.setText(String.format("£%.2f", item.price));
                    Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Validate and format price input
    private boolean validateAndSetPrice(EditText priceEt, TextView priceTv) {
        try {
            double price = Double.parseDouble(priceEt.getText().toString());
            if (price < 0) {
                return false;
            }
            priceTv.setText(String.format("£%.2f", price));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Setup replace image button
    private void setupReplaceImageButton(View replaceImageButton) {
        if (replaceImageButton == null) return;

        replaceImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    // setup save button with validation and data update
    private void setupSaveButton(
            Button saveButton, Dialog dialog, MenuItem item,
            EditText nameEt, EditText descriptionEt, EditText priceEt
    ) {
        if (saveButton == null) return;

        saveButton.setOnClickListener(v -> {
              // Validate and collect data
            String newTitle = nameEt != null ? nameEt.getText().toString().trim() : item.title;
            String newDescription = descriptionEt != null ? descriptionEt.getText().toString().trim() : item.description;
            double newPrice;

            try {
                newPrice = priceEt != null ? Double.parseDouble(priceEt.getText().toString()) : item.price;
                if (newPrice < 0) {
                    Toast.makeText(this, "Price cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
                return;
            }

            // validate required fields
            if (newTitle.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            //Update item
            updateMenuItem(item, newTitle, newDescription, newPrice);

            // Save to database
            saveMenuItemChanges(item);

            Toast.makeText(this, "Changes saved (local only - Firebase not connected)", Toast.LENGTH_SHORT).show();

            refreshMenuDisplay();
            dialog.dismiss();
        });
    }

    // setup delete button
    private void setupDeleteButton(Button deleteButton, Dialog dialog, MenuItem item) {
        if (deleteButton == null) return;

        deleteButton.setOnClickListener(v -> {
            // delete from database
            deleteMenuItem(item);

            Toast.makeText(this, "Item deleted (local only - Firebase not connected)", Toast.LENGTH_SHORT).show();

            refreshMenuDisplay();
            dialog.dismiss();
        });
    }

    // update menu item data
    private void updateMenuItem(MenuItem item, String title, String description, double price) {
        item.title = title;
        item.description = description;
        item.price = price;

        // If new image was selected, prepare for upload
        if (selectedImageUri != null) {
            // TODO: Upload to Firebase Storage
            // item.imageUrl = uploadedUrl;
        }
    }

    // save menu item changes
    private void saveMenuItemChanges(MenuItem item) {
        // to do later: implement Firebase save
    }

    // delete menu item
    private void deleteMenuItem(MenuItem item) {
        // I will do later: implement Firebase delete
    }

     // refresh menu display
    private void refreshMenuDisplay() {
        List<MenuItem> menuItems = getFakeMenuData();
        displayMenuItems(menuItems);
    }

    // Will do later: database (firebase) methods to do later

    private void uploadImageToFirebase(Uri imageUri, String itemId) {
        // upload the image to database storage
        // get the download url of the image
        //update the item's image url field
    }

    private void saveMenuItemToFirebase(MenuItem item) {
        // save or update menu item in Firebase Realtime Database or Firestore
        // use item.itemId as document/key ID
    }

    private void deleteMenuItemFromFirebase(String itemId) {
        // delete menu item from the database
        // also delete associated image from Storage
    }

    private void fetchMenuItemsFromFirebase() {
        //Fetch the menu items from database
        // I will alter replace getFakeMenuData() with this method
    }
}
