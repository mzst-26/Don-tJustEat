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


}

