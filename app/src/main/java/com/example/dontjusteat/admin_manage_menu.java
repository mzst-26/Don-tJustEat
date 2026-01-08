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

import com.example.dontjusteat.repositories.MenuRepository;

import java.util.ArrayList;
import java.util.List;

public class admin_manage_menu extends BaseActivity {

    // menu items list
    private final List<MenuItem> menuItemsData = new ArrayList<>();
    // image picker stuff
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView currentEditingImageView;
    private Uri selectedImageUri;
    // restaurant id for this admin
    private String restaurantId;
    // repository for menu operations
    private MenuRepository menuRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ensure only admins can view this screen
        if (!requireAdminOrFinish()) {
            return;
        }
        setContentView(R.layout.admin_manage_menu);

        // initialize repository
        menuRepository = new MenuRepository();

        // bind header views
        bindHeaderInfo();

        // initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // Get selected image URI
                        selectedImageUri = result.getData().getData();
                        android.util.Log.d("ADMIN_MENU", "Image selected - URI: " + selectedImageUri);
                        if (currentEditingImageView != null && selectedImageUri != null) {
                            // Display selected image in the popup
                            currentEditingImageView.setImageURI(selectedImageUri);
                            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        android.util.Log.d("ADMIN_MENU", "Image selection cancelled or failed");
                    }
                }
        );

        //import modules
        Modules.applyWindowInsets(this, R.id.rootView);
        admin_modules.handleMenuNavigation(this);
        Modules.handleSimpleHeaderNavigation(this);

        // add button
        ImageView addButton = findViewById(R.id.button_add_new_menu_item);
        if (addButton != null) {
            addButton.setOnClickListener(v -> handleAddNewMenuItem()); // open add popup
        }

        // load data from firestore
        menuItemsData.clear();
        loadMenuFromFirestore();
    }

    private void bindHeaderInfo() {
        // find header views
        TextView headerTitle = findViewById(R.id.header_location_name);
        ImageView headerImage = findViewById(R.id.right_header_image);

        // fetch restaurant for this admin and update header
        com.example.dontjusteat.repositories.AdminBookingRepository adminRepo = new com.example.dontjusteat.repositories.AdminBookingRepository();
        adminRepo.getAdminRestaurantId(new com.example.dontjusteat.repositories.AdminBookingRepository.OnAdminRestaurantListener() {
            @Override
            public void onSuccess(String rid) {


                restaurantId = rid;
                com.example.dontjusteat.repositories.RestaurantRepository repo = new com.example.dontjusteat.repositories.RestaurantRepository();
                repo.getRestaurantById(rid, new com.example.dontjusteat.repositories.RestaurantRepository.OnRestaurantFetchListener() {
                    @Override

                    public void onSuccess(com.example.dontjusteat.models.Restaurant r) {
                        if (headerTitle != null && r != null && r.getName() != null) {
                            headerTitle.setText(r.getName());
                        }

                        if (headerImage != null && r != null && r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
                            try {
                                com.bumptech.glide.Glide.with(admin_manage_menu.this).load(r.getImageUrl()).into(headerImage);
                            } catch (Exception ignored) {
                            }
                        }


                    }

                    @Override
                    public void onFailure(String error) {
                        android.util.Log.e("ADMIN_MENU", "header load failed: " + error);
                    }


                });
            }

            @Override
            public void onFailure(String error) {

                android.util.Log.e("ADMIN_MENU", "no restaurant id for header: " + error);
            }
        });
    }

    // Display menu items in the GridLayout
    private void displayMenuItems(List<MenuItem> menuItems) {
        GridLayout container = findViewById(R.id.menu_items_container);
        TextView emptyState = findViewById(R.id.menu_empty_state);
        if (container == null) {
            android.util.Log.e("admin_manage_menu", "GridLayout container not found!");
            return;
        }

        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        // empty state
        if (menuItems == null || menuItems.isEmpty()) {
            container.setVisibility(View.GONE);
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            return;
        } else {
            container.setVisibility(View.VISIBLE);
            if (emptyState != null) emptyState.setVisibility(View.GONE);
        }

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
                if (title != null) title.setText(item.title);

                // image from url if present; fallback
                try {
                    if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                        com.bumptech.glide.Glide.with(this).load(item.imageUrl).into(image);
                    } else if (image != null) {
                        image.setImageResource(item.imageResource != 0 ? item.imageResource : R.drawable.restaurant_image);
                    }
                } catch (Exception ignored) {
                }

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
        bindDialogViews(dialog, item, false); // edit mode
        dialog.show();
    }

    // handle the add new menu item button
    private void handleAddNewMenuItem() {
        MenuItem newItem = new MenuItem(
                generateNewItemId(), // temporary id (later: Firebase id)
                "",
                "",
                0.0,
                R.drawable.pink_back_minimum_radius // placeholder
        );
        Dialog dialog = createMenuItemDialog();
        bindDialogViews(dialog, newItem, true); // add mode
        dialog.show();
    }

    // generate new item id (this is temporary, later I will use Firebase generated ids)
    private String generateNewItemId() {
        return "ITEM" + String.format("%03d", (int) (Math.random() * 1000));
    }

    // build dialog (simple)
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
    private void bindDialogViews(Dialog dialog, MenuItem item, boolean isAddMode) {
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

        // Setup image picker reference
        currentEditingImageView = itemImage;
        android.util.Log.d("ADMIN_MENU", "Dialog opened - resetting selectedImageUri (was: " + selectedImageUri + ")");
        selectedImageUri = null;

        if (isAddMode) {
            // add: show edits, hide icons
            configureAddMode(nameTv, nameEt, editNameIcon, descriptionTv, descriptionEt,
                    editDescriptionIcon, priceTv, priceEt, editPriceIcon, deleteButton);
        } else {
            // edit: toggle by icon
            setupEditToggle(nameTv, nameEt, editNameIcon);
            setupEditToggle(descriptionTv, descriptionEt, editDescriptionIcon);
            setupPriceEditToggle(priceTv, priceEt, editPriceIcon, item);
            if (deleteButton != null) deleteButton.setText("Delete"); // simple
        }

        // buttons
        setupReplaceImageButton(replaceImageButton);
        setupSaveButton(saveButton, dialog, item, nameEt, descriptionEt, priceEt, isAddMode);
        setupDeleteOrCancelButton(deleteButton, dialog, item, isAddMode);
    }

    // add mode: make fields editable
    private void configureAddMode(
            TextView nameTv, EditText nameEt, ImageView editNameIcon,
            TextView descriptionTv, EditText descriptionEt, ImageView editDescriptionIcon,
            TextView priceTv, EditText priceEt, ImageView editPriceIcon,
            Button deleteButton) {

        // hide textviews, show edits, hide icons
        if (nameTv != null) nameTv.setVisibility(View.GONE);
        if (nameEt != null) {
            nameEt.setVisibility(View.VISIBLE);
            nameEt.setHint("Enter item name");
        }
        if (editNameIcon != null) editNameIcon.setVisibility(View.GONE);


        // description
        if (descriptionTv != null) descriptionTv.setVisibility(View.GONE);
        if (descriptionEt != null) {
            descriptionEt.setVisibility(View.VISIBLE);
            descriptionEt.setHint("Enter description");
        }
        if (editDescriptionIcon != null) editDescriptionIcon.setVisibility(View.GONE);

        // price
        if (priceTv != null) priceTv.setVisibility(View.GONE);
        if (priceEt != null) {
            priceEt.setVisibility(View.VISIBLE);
            priceEt.setHint("Enter price");
        }
        if (editPriceIcon != null) editPriceIcon.setVisibility(View.GONE);

        // delete button acts as cancel
        if (deleteButton != null) deleteButton.setText("Cancel"); // no delete on new
    }

    // fill the dialog with current item data
    private void populateDialogData(MenuItem item, ImageView itemImage, TextView nameTv,
                                    EditText nameEt, TextView descriptionTv, EditText descriptionEt,
                                    TextView priceTv, EditText priceEt) {
        // load image with Glide if URL exists, otherwise use resource
        if (itemImage != null) {
            if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(this).load(item.imageUrl).into(itemImage);
            } else {
                itemImage.setImageResource(item.imageResource != 0 ? item.imageResource : R.drawable.restaurant_image);
            }
        }
        if (nameTv != null) nameTv.setText(item.title);
        if (nameEt != null) nameEt.setText(item.title);
        if (descriptionTv != null) descriptionTv.setText(item.description);
        if (descriptionEt != null) descriptionEt.setText(item.description);
        if (priceTv != null) priceTv.setText(String.format("£%.2f", item.price));
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
            EditText nameEt, EditText descriptionEt, EditText priceEt, boolean isAddMode
    ) {
        if (saveButton == null) return;
        saveButton.setOnClickListener(v -> {
            // Validate and collect data
            String newTitle = nameEt != null ? nameEt.getText().toString().trim() : item.title;
            String newDescription = descriptionEt != null ? descriptionEt.getText().toString().trim() : item.description;
            double newPrice;

            //toasts for price input
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


            //some toasts for empty fields
            if (newTitle.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newDescription.isEmpty()) {
                Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPrice == 0.0 && isAddMode) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show();
                return;
            }

            // update item data
            item.title = newTitle;
            item.description = newDescription;
            item.price = newPrice;

            // add to list if new
            if (isAddMode) {
                menuItemsData.add(item);
            }

            // show loading
            android.app.Dialog loading = LoadingOverlay.show(this);

            // save using repository
            android.util.Log.d("ADMIN_MENU", "=== SAVE CLICKED ===");
            android.util.Log.d("ADMIN_MENU", "restaurantId: " + restaurantId);
            android.util.Log.d("ADMIN_MENU", "selectedImageUri: " + selectedImageUri);
            android.util.Log.d("ADMIN_MENU", "item.imageUrl: " + item.imageUrl);
            android.util.Log.d("ADMIN_MENU", "item.itemId: " + item.itemId);

            if (restaurantId == null || restaurantId.isEmpty()) {
                Toast.makeText(this, "Error: Restaurant ID is missing!", Toast.LENGTH_LONG).show();
                android.util.Log.e("ADMIN_MENU", "Cannot save - restaurantId is null or empty!");
                return;
            }

            Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();
            menuRepository.saveMenuItemWithImage(
                    restaurantId,
                    item.itemId,
                    newTitle,
                    newDescription,
                    newPrice,
                    selectedImageUri,
                    item.imageUrl,
                    new MenuRepository.OnMenuItemSaveListener() {
                        @Override
                        public void onSuccess() {
                            LoadingOverlay.hide(loading);
                            Toast.makeText(admin_manage_menu.this, "Menu item saved successfully", Toast.LENGTH_SHORT).show();
                            selectedImageUri = null;
                            loadMenuFromFirestore();
                            dialog.dismiss();
                        }

                        @Override
                        public void onFailure(String error) {
                            LoadingOverlay.hide(loading);
                            Toast.makeText(admin_manage_menu.this, "Save failed: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        });
    }

    // delete / cancel
    private void setupDeleteOrCancelButton(Button deleteButton, Dialog dialog, MenuItem item, boolean isAddMode) {
        if (deleteButton == null) return;
        deleteButton.setOnClickListener(v -> {
            if (isAddMode) {
                dialog.dismiss();
            } else {
                deleteMenuItem(item);
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                refreshMenuDisplay();
                dialog.dismiss();
            }
        });
    }

    // delete menu item
    private void deleteMenuItem(MenuItem item) {
        // remove from local list
        for (int i = 0; i < menuItemsData.size(); i++) {
            if (menuItemsData.get(i).itemId.equals(item.itemId)) {
                menuItemsData.remove(i);
                break;
            }
        }

        android.app.Dialog loading = LoadingOverlay.show(this);

        if (restaurantId != null && item.itemId != null) {
            menuRepository.deleteMenuItem(restaurantId, item.itemId, new MenuRepository.OnMenuItemDeleteListener() {
                @Override
                public void onSuccess() {
                    LoadingOverlay.hide(loading);
                    android.util.Log.d("ADMIN_MENU", "Menu item deleted successfully");
                }

                @Override
                public void onFailure(String error) {
                    LoadingOverlay.hide(loading);
                    android.util.Log.e("ADMIN_MENU", "Failed to delete: " + error);
                }

            });
        } else {
            LoadingOverlay.hide(loading);
        }
    }

    // redraw the menu display
    private void refreshMenuDisplay() {
        displayMenuItems(menuItemsData);
    }


    // load real menu from firestore for admin's restaurant
    private void loadMenuFromFirestore() {
        com.example.dontjusteat.repositories.AdminBookingRepository adminRepo = new com.example.dontjusteat.repositories.AdminBookingRepository();
        adminRepo.getAdminRestaurantId(new com.example.dontjusteat.repositories.AdminBookingRepository.OnAdminRestaurantListener() {
            @Override
            public void onSuccess(String rid) {
                restaurantId = rid;
                com.example.dontjusteat.repositories.RestaurantRepository repo = new com.example.dontjusteat.repositories.RestaurantRepository();
                repo.getMenuItemsByRestaurantId(restaurantId, new com.example.dontjusteat.repositories.RestaurantRepository.OnMenuItemsListener() {
                    @Override
                    public void onSuccess(List<com.example.dontjusteat.models.MenuItem> items) {
                        // map to UI model
                        menuItemsData.clear();
                        if (items != null) {
                            for (com.example.dontjusteat.models.MenuItem it : items) {
                                MenuItem ui = new MenuItem();
                                ui.itemId = it.getItemId();
                                ui.title = it.getItemName();
                                ui.description = it.getItemDes();
                                ui.price = it.getPrice();
                                ui.imageResource = R.drawable.restaurant_image;
                                ui.imageUrl = it.getImageURL();
                                menuItemsData.add(ui);
                            }
                        }
                        refreshMenuDisplay();
                    }

                    @Override
                    public void onFailure(String error) {
                        android.util.Log.e("ADMIN_MENU", "Failed to load menu: " + error);
                        Toast.makeText(admin_manage_menu.this, "Failed to load menu", Toast.LENGTH_SHORT).show();
                        // show empty state
                        menuItemsData.clear();
                        refreshMenuDisplay();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                android.util.Log.e("ADMIN_MENU", "No restaurant id: " + error);
                Toast.makeText(admin_manage_menu.this, "Restaurant not set for admin", Toast.LENGTH_SHORT).show();
                // empty state
                menuItemsData.clear();
                refreshMenuDisplay();
            }
        });
    }

    // menu item class
    public static class MenuItem {
        public String itemId;
        public String title;
        public String description;
        public double price;
        public int imageResource;
        public String imageUrl;

        // empty constructor for Firebase
        public MenuItem() {
        }

        MenuItem(String itemId, String title, String description, double price, int imageResource) {
            this.itemId = itemId;
            this.title = title;
            this.description = description;
            this.price = price;
            this.imageResource = imageResource;
            this.imageUrl = null; // Will be populated when connected to databse
        }
    }
}
