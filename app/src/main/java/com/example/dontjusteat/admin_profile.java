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
import com.google.firebase.auth.FirebaseAuth;


public class admin_profile extends AppCompatActivity {

    private ImageView profileImageView;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private boolean isEditingName = false;
    private boolean isEditingPhone = false;
    private ProfileEditHelper editHelper;
    private PreferencesRepository preferencesRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile);

        editHelper = new ProfileEditHelper(this);
        preferencesRepository = new PreferencesRepository(this);

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        Modules.applyWindowInsets(this, R.id.rootView);
        admin_modules.handleMenuNavigation(this);

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

        TextView tvAdminLocation = findViewById(R.id.tv_admin_location);
        if (tvAdminLocation != null) {
            tvAdminLocation.setText("Location: Plymouth, UK");
        }
    }

    private void setupNameEditing() {
        TextView tvAdminName = findViewById(R.id.tv_admin_name);
        EditText etAdminName = findViewById(R.id.et_admin_name);
        ImageView editNameButton = findViewById(R.id.admin_name_edit_button);

        boolean[] isEditing = {isEditingName};
        editHelper.setupNameEditing(tvAdminName, etAdminName, editNameButton, isEditing);
        isEditingName = isEditing[0];
    }

    private void setupPhoneEditing() {
        TextView tvAdminPhone = findViewById(R.id.tv_admin_phone);
        EditText etAdminPhone = findViewById(R.id.et_admin_phone);
        ImageView editPhoneButton = findViewById(R.id.admin_phone_edit_button);

        boolean[] isEditing = {isEditingPhone};
        editHelper.setupPhoneEditing(tvAdminPhone, etAdminPhone, editPhoneButton, isEditing);
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
        Switch secondaryUpdatesSwitch = findViewById(R.id.secondary_updates_toggle);

        preferencesRepository.loadPreferences(new PreferencesRepository.OnPreferencesLoadListener() {
            @Override
            public void onSuccess(UserPreferences preferences) {
                secondaryUpdatesSwitch.setChecked(preferences.isSecondaryUpdates());
            }

            @Override
            public void onFailure(String error) {
                secondaryUpdatesSwitch.setChecked(false);
            }

            @Override
            public void onPreferencesNotFound() {
                secondaryUpdatesSwitch.setChecked(false);
            }
        });

        secondaryUpdatesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesRepository.updateSecondaryUpdates(isChecked, new PreferencesRepository.OnPreferencesActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(admin_profile.this, "Preference updated", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String error) {
                    secondaryUpdatesSwitch.setChecked(!isChecked);
                    Toast.makeText(admin_profile.this, "Failed to update preference", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
