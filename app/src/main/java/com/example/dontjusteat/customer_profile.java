package com.example.dontjusteat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.dontjusteat.helpers.ProfileEditHelper;
import com.example.dontjusteat.models.UserPreferences;
import com.example.dontjusteat.repositories.PreferencesRepository;
import com.example.dontjusteat.repositories.UserProfileRepository;


public class customer_profile extends AppCompatActivity {

    private ImageView profileImageView;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private boolean isEditingName = false;
    private boolean isEditingPhone = false;
    private ProfileEditHelper editHelper;
    private PreferencesRepository preferencesRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_profile);

        editHelper = new ProfileEditHelper(this);
        preferencesRepository = new PreferencesRepository(this);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        Modules.applyWindowInsets(this, R.id.rootView);
        Modules.handleMenuNavigation(this);

        View profileImageContainer = findViewById(R.id.profile_image_container);
        profileImageView = findViewById(R.id.profile_image_view);
        ImageView editIcon = findViewById(R.id.editIcon);

        editHelper.loadInitialPhoto(profileImageView);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                String uriString = uri.toString();
                setUserImage(uriString);
                editHelper.savePhoto(uriString);
            }
        });

        if (profileImageContainer != null) {
            editIcon.setOnClickListener(v -> openImagePicker());
        }

        setupNameEditing();
        setupPhoneEditing();
        setupPreferences();
        handleLogout();
    }

    private void setupNameEditing() {
        TextView tvCustomerName = findViewById(R.id.tv_customer_name);
        EditText etCustomerName = findViewById(R.id.et_customer_name);
        ImageView editNameButton = findViewById(R.id.customer_name_edit_button);

        boolean[] isEditing = {isEditingName};
        editHelper.setupNameEditing(tvCustomerName, etCustomerName, editNameButton, isEditing);
        isEditingName = isEditing[0];
    }

    private void setupPhoneEditing() {
        TextView tvPhoneNumber = findViewById(R.id.tv_customer_phone);
        EditText etPhoneNumber = findViewById(R.id.et_customer_phone);
        ImageView editPhoneButton = findViewById(R.id.customer_phone_edit_button);

        boolean[] isEditing = {isEditingPhone};
        editHelper.setupPhoneEditing(tvPhoneNumber, etPhoneNumber, editPhoneButton, isEditing);
        isEditingPhone = isEditing[0];
    }

    private void handleLogout() {
        View logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> Modules.logoutAction(this));
    }

    private void openImagePicker() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void setUserImage(String url) {
        if (profileImageView != null) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.profile_floating_disc)
                    .error(R.drawable.profile_floating_disc)
                    .circleCrop()
                    .into(profileImageView);
        }
    }

    private void setupPreferences() {
        Switch offersSwitch = findViewById(R.id.switch_offers_discount);
        Switch secondaryUpdatesSwitch = findViewById(R.id.secondary_updates_toggle);

        preferencesRepository.loadPreferences(new PreferencesRepository.OnPreferencesLoadListener() {
            @Override
            public void onSuccess(UserPreferences preferences) {
                offersSwitch.setChecked(preferences.isOffersAndDiscounts());
                secondaryUpdatesSwitch.setChecked(preferences.isSecondaryUpdates());
            }

            @Override
            public void onFailure(String error) {
                offersSwitch.setChecked(false);
                secondaryUpdatesSwitch.setChecked(false);
            }

            @Override
            public void onPreferencesNotFound() {
                offersSwitch.setChecked(false);
                secondaryUpdatesSwitch.setChecked(false);
            }
        });

        offersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesRepository.updateOffersAndDiscounts(isChecked, new PreferencesRepository.OnPreferencesActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(customer_profile.this, "Preference updated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    offersSwitch.setChecked(!isChecked);
                    Toast.makeText(customer_profile.this, "Failed to update preference", Toast.LENGTH_SHORT).show();
                }
            });
        });

        secondaryUpdatesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesRepository.updateSecondaryUpdates(isChecked, new PreferencesRepository.OnPreferencesActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(customer_profile.this, "Preference updated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    secondaryUpdatesSwitch.setChecked(!isChecked);
                    Toast.makeText(customer_profile.this, "Failed to update preference", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
