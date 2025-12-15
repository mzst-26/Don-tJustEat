package com.example.dontjusteat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;


public class customer_profile extends AppCompatActivity {

    private ImageView profileImageView;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

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
}
