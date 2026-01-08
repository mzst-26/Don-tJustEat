package com.example.dontjusteat.repositories;

import android.net.Uri;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MenuRepository {

    private final FirebaseFirestore db;
    private final ImageUploadRepository imageUploadRepo;

    public MenuRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.imageUploadRepo = new ImageUploadRepository();

    }


    // save or update menu item
    public void saveMenuItem(String restaurantId, String itemId, String itemName, String itemDescription,
                             double price, String imageUrl, OnMenuItemSaveListener listener) {
        if (restaurantId == null || restaurantId.isEmpty()) {
            listener.onFailure("Restaurant ID is required");

            return;
        }


        // generate new ID if needed
        String finalItemId = itemId;


        if (finalItemId == null || finalItemId.isEmpty() || finalItemId.startsWith("ITEM")) {
            finalItemId = db.collection("restaurants")
                    .document(restaurantId)
                    .collection("menu")
                    .document()
                    .getId();
        }


        // prepare data
        Map<String, Object> menuData = new HashMap<>();

        menuData.put("ItemName", itemName);
        menuData.put("ItemDes", itemDescription);
        menuData.put("Price", price);


        if (imageUrl != null && !imageUrl.isEmpty()) {
            menuData.put("imageURL", imageUrl);
        }

        // save to Firestore
        db.collection("restaurants")
                .document(restaurantId)
                .collection("menu")
                .document(finalItemId)
                .set(menuData)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> {
                    // android.util.Log.e("MenuRepository", "Failed to save menu item: " + e.getMessage());
                    listener.onFailure("Failed to save menu item: " + e.getMessage());
                });
    }


    // save menu item with image upload
    public void saveMenuItemWithImage(String restaurantId, String itemId, String itemName,
                                      String itemDescription, double price, Uri imageUri,
                                      String existingImageUrl, OnMenuItemSaveListener listener) {
        // android.util.Log.d("MenuRepository", "saveMenuItemWithImage called - hasNewImage: " + (imageUri != null) + ", existingImageUrl: " + existingImageUrl);

        if (imageUri == null) {
            // no new image, use existing image URL (or null if none)
            // android.util.Log.d("MenuRepository", "No new image, saving with existing URL");
            saveMenuItem(restaurantId, itemId, itemName, itemDescription, price, existingImageUrl, listener);
            return;

        }

        // upload new image first

        // android.util.Log.d("MenuRepository", "Uploading new image...");
        imageUploadRepo.uploadMenuImage(restaurantId, itemId, imageUri, new ImageUploadRepository.OnImageUploadListener() {
            @Override
            public void onSuccess(String downloadUrl) {
                // android.util.Log.d("MenuRepository", "Image uploaded, saving menu item with URL: " + downloadUrl);

                // save with new image URL
                saveMenuItem(restaurantId, itemId, itemName, itemDescription, price, downloadUrl, listener);
            }

            @Override
            public void onFailure(String error) {
                // android.util.Log.e("MenuRepository", "Image upload failed: " + error);
                listener.onFailure(error);
            }
        });
    }


    // delete menu item
    public void deleteMenuItem(String restaurantId, String itemId, OnMenuItemDeleteListener listener) {
        if (restaurantId == null || itemId == null) {
            listener.onFailure("Restaurant ID or Item ID is missing");
            return;
        }


        db.collection("restaurants")
                .document(restaurantId)
                .collection("menu")
                .document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> {

                    // android.util.Log.d("MenuRepository", "Menu item deleted successfully");
                    listener.onSuccess();
                })

                .addOnFailureListener(e -> {
                    // android.util.Log.e("MenuRepository", "Failed to delete menu item: " + e.getMessage());
                    listener.onFailure("Failed to delete menu item: " + e.getMessage());
                });
    }


    public interface OnMenuItemSaveListener {
        void onSuccess();

        void onFailure(String error);
    }

    public interface OnMenuItemDeleteListener {
        void onSuccess();

        void onFailure(String error);
    }
}

