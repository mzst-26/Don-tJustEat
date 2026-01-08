package com.example.dontjusteat.repositories;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;


public class ImageUploadRepository {

    private static final String TAG = "ImageUploadRepository";
    private final FirebaseStorage storage;

    public ImageUploadRepository() {
        this.storage = FirebaseStorage.getInstance();
    }

    // upload image to a specific path and return download URL
    public void uploadImage(Uri imageUri, String path, OnImageUploadListener listener) {
        if (imageUri == null) {
            Log.e(TAG, "Upload failed: Image URI is null");
            listener.onFailure("Image URI is null");
            return;
        }

        if (path == null || path.isEmpty()) {
            Log.e(TAG, "Upload failed: Path is empty");
            listener.onFailure("Path is empty");
            return;
        }

        // create unique filename
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileRef = storage.getReference().child(path).child(fileName);

        Log.d(TAG, "Starting upload to: " + path + "/" + fileName);

        // upload image
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "File uploaded, getting download URL...");
                    // get download URL


                    fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String downloadUrl = uri.toString();

                                Log.d(TAG, "Image uploaded successfully: " + downloadUrl);
                                listener.onSuccess(downloadUrl);
                            })

                            .addOnFailureListener(e -> {

                                Log.e(TAG, "Failed to get download URL: " + e.getMessage(), e);
                                listener.onFailure("Failed to get image URL: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed: " + e.getMessage(), e);
                    listener.onFailure("Image upload failed: " + e.getMessage());
                });


    }

    // upload menu item image
    public void uploadMenuImage(String restaurantId, String itemId, Uri imageUri, OnImageUploadListener listener) {
        String path = "restaurants/" + restaurantId + "/menu";
        Log.d(TAG, "Uploading menu image for restaurant: " + restaurantId);

        uploadImage(imageUri, path, listener);
    }

    // upload restaurant profile image
    public void uploadRestaurantImage(String restaurantId, Uri imageUri, OnImageUploadListener listener) {
        String path = "restaurants/" + restaurantId + "/profile";
        uploadImage(imageUri, path, listener);
    }

    // upload user profile image
    public void uploadUserProfileImage(String userId, Uri imageUri, OnImageUploadListener listener) {

        String path = "users/" + userId + "/profile";
        uploadImage(imageUri, path, listener);

    }

    // delete image from storage
    public void deleteImage(String imageUrl, OnImageDeleteListener listener) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            listener.onFailure("Image URL is empty");
            return;
        }


        try {
            StorageReference imageRef = storage.getReferenceFromUrl(imageUrl);
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Image deleted successfully");
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete image: " + e.getMessage());
                        listener.onFailure("Failed to delete image: " + e.getMessage());
                    });

        } catch (Exception e) {
            Log.e(TAG, "Invalid image URL: " + e.getMessage());
            listener.onFailure("Invalid image URL");
        }
    }

    public interface OnImageUploadListener {
        void onSuccess(String downloadUrl);

        void onFailure(String error);
    }

    public interface OnImageDeleteListener {
        void onSuccess();

        void onFailure(String error);
    }
}

