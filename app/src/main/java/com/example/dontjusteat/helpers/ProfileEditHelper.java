package com.example.dontjusteat.helpers;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.example.dontjusteat.R;
import com.example.dontjusteat.repositories.UserProfileRepository;

public class ProfileEditHelper {
    private final UserProfileRepository profileRepository;
    private final android.app.Activity activity;

    public ProfileEditHelper(android.app.Activity activity) {
        this.activity = activity;
        this.profileRepository = new UserProfileRepository(activity);
    }

    public void setupNameEditing(TextView tvName, EditText etName, ImageView btnEdit, boolean[] isEditing) {
        profileRepository.loadUserProfile(new UserProfileRepository.OnProfileLoadListener() {
            @Override
            public void onSuccess(String name, String phone, String photoUrl) {
                if (name != null && !name.isEmpty()) {
                    tvName.setText(name);
                }
            }

            @Override
            public void onFailure(String error) {
                tvName.setText("User");
            }
        });

        btnEdit.setOnClickListener(v -> {
            if (isEditing[0]) {
                String newName = etName.getText().toString().trim();
                if (!newName.isEmpty()) {
                    tvName.setText(newName);
                    profileRepository.updateUserName(newName, new UserProfileRepository.OnProfileUpdateListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(activity, "Name updated", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(activity, "Failed to update name", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                tvName.setVisibility(View.VISIBLE);
                etName.setVisibility(View.GONE);
                btnEdit.setImageResource(R.drawable.edit_note);
                isEditing[0] = false;
            } else {
                etName.setText(tvName.getText());
                tvName.setVisibility(View.GONE);
                etName.setVisibility(View.VISIBLE);
                etName.requestFocus();
                btnEdit.setImageResource(R.drawable.save_icon);
                isEditing[0] = true;
            }
        });
    }

    public void setupPhoneEditing(TextView tvPhone, EditText etPhone, ImageView btnEdit, boolean[] isEditing) {
        profileRepository.loadUserProfile(new UserProfileRepository.OnProfileLoadListener() {
            @Override
            public void onSuccess(String name, String phone, String photoUrl) {
                if (phone != null && !phone.isEmpty()) {
                    tvPhone.setText(phone);
                }
            }

            @Override
            public void onFailure(String error) {
                tvPhone.setText("");
            }
        });

        btnEdit.setOnClickListener(v -> {
            if (isEditing[0]) {
                String newPhone = etPhone.getText().toString().trim();
                if (!newPhone.isEmpty()) {
                    tvPhone.setText(newPhone);
                    profileRepository.updateUserPhone(newPhone, new UserProfileRepository.OnProfileUpdateListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(activity, "Phone updated", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(activity, "Failed to update phone", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                tvPhone.setVisibility(View.VISIBLE);
                etPhone.setVisibility(View.GONE);
                btnEdit.setImageResource(R.drawable.edit_note);
                isEditing[0] = false;
            } else {
                etPhone.setText(tvPhone.getText());
                tvPhone.setVisibility(View.GONE);
                etPhone.setVisibility(View.VISIBLE);
                etPhone.requestFocus();
                btnEdit.setImageResource(R.drawable.save_icon);
                isEditing[0] = true;
            }
        });
    }

    private void displayImage(String url, android.widget.ImageView imageView) {
        if (imageView == null) return;

        com.bumptech.glide.Glide.with(activity.getApplicationContext())
                .load(url)
                .placeholder(R.drawable.profile_floating_disc)
                .error(R.drawable.profile_floating_disc)
                .circleCrop()
                .into(imageView);
    }

    public void loadInitialPhoto(android.widget.ImageView imageView) {
        profileRepository.loadUserProfile(new UserProfileRepository.OnProfileLoadListener() {
            @Override
            public void onSuccess(String name, String phone, String photoUrl) {
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    displayImage(photoUrl, imageView);
                }
            }

            @Override
            public void onFailure(String error) {
            }
        });
    }

    public void savePhoto(String photoUrl) {
        if (photoUrl == null || photoUrl.isEmpty()) return;

        profileRepository.updateUserPhoto(photoUrl, new UserProfileRepository.OnProfileUpdateListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(activity, "Photo updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(activity, "Failed to update photo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

