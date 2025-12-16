package com.example.dontjusteat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;


public class customer_profile extends AppCompatActivity {

    private ImageView profileImageView;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private boolean isEditingName = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_profile);

        //Back button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Apply window insets
        Modules.applyWindowInsets(this, R.id.rootView);

        // Handle menu navigation
        Modules.handleMenuNavigation(this);

        //logic that handles the user profile
        // initialize the UI components
        View profileImageContainer = findViewById(R.id.profile_image_container);
        profileImageView = findViewById(R.id.profile_image_view);
        ImageView editIcon = findViewById(R.id.editIcon);

        //Load saved image on start to get the user image
        loadSavedImage();

        // register the photo picker activity result launcher
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                // User selected a new image
                String uriString = uri.toString();
                setUserImage(uriString); // Update UI immediately
                saveImageToLocal(uriString); // Save the new URI to the local storage. Later I will replace this with a network call to the server
            } else {
                // The user ignored the picker
            }
        });

        // Set click listener on the container to open the picker
        if (profileImageContainer != null) {
            editIcon.setOnClickListener(v -> openImagePicker());
        }

        setupNameEditing();
        handleLogout();

    }
    private void setupNameEditing() {
        TextView tvCustomerName = findViewById(R.id.tv_customer_name);
        EditText etCustomerName = findViewById(R.id.et_customer_name);
        ImageView editNameButton = findViewById(R.id.customer_name_edit_button);

        // Load saved name
        loadSavedName(tvCustomerName);

        editNameButton.setOnClickListener(v -> {
            if (isEditingName) {
                // switch between save mode to the view mode
                String newName = etCustomerName.getText().toString().trim();
                if (!newName.isEmpty()) {
                    tvCustomerName.setText(newName);
                    saveName(newName);
                }

                tvCustomerName.setVisibility(View.VISIBLE);
                etCustomerName.setVisibility(View.GONE);
                editNameButton.setImageResource(R.drawable.edit_note); // Change icon back to edit
                isEditingName = false;

            } else {
                // view mode to edit mode
                etCustomerName.setText(tvCustomerName.getText());
                tvCustomerName.setVisibility(View.GONE);
                etCustomerName.setVisibility(View.VISIBLE);
                etCustomerName.requestFocus();
                // Optionally show keyboard here if needed

                editNameButton.setImageResource(R.drawable.save_icon); // Change icon to save/check
                isEditingName = true;
            }
        });
    }

    private void handleLogout() {
        // Handle logout button click
        View logoutButton = findViewById(R.id.logout_button);
        // Set click listener on the logout button and handle the logout action
        logoutButton.setOnClickListener(v -> logoutAction(this));
    }

    private void saveName(String name) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("customer_name", name);
        editor.apply();
    }

    private void loadSavedName(TextView tvCustomerName) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedName = sharedPreferences.getString("customer_name", "Jack Smith"); // Default name
        tvCustomerName.setText(savedName);
    }

    //launch the android photo picker
    private void openImagePicker() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                // only show the images
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    // this method is used to load image using Glide
    private void setUserImage(String url) {
        if (profileImageView != null) {
            //Load the image using Glide
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.profile_floating_disc)
                    .error(R.drawable.profile_floating_disc)
                    .circleCrop()
                    .into(profileImageView);
        }
    }


    //save the image to the local storage
    private void saveImageToLocal(String url) {
        if (url == null) return;

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profile_image_uri", url);
        editor.apply();
    }

    private void loadSavedImage() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedUri = sharedPreferences.getString("profile_image_uri", null);

        if (savedUri != null) {
            // Load the saved image into the view
            setUserImage(savedUri);
        }
    }

    private void logoutAction(Activity activity){
        // for now we just navigate to the mainActivity then later will add the logout logic
        Intent intent = new Intent(activity, MainActivity.class);
        startActivities(new Intent[]{intent});


    }
}
