package com.example.dontjusteat.repositories;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.dontjusteat.models.Restaurant;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RestaurantRepository {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private final Context context;



    // create the constructor
    public RestaurantRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.context = context;
    }
    // get all the restaurants
    public void loadAllRestaurants(@NonNull OnRestaurantsLoadListener listener) {
        db.collection("restaurants")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Restaurant> restaurants = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        if (Boolean.TRUE.equals(doc.getBoolean("isActive"))){
                            Restaurant r = doc.toObject(Restaurant.class);

                            //store document id in model
                            r.setId(doc.getId());

                            restaurants.add(r);
                        }
                    }
                    listener.onSuccess(restaurants);
                })
                .addOnFailureListener(e -> listener.onFailure(
                        e.getMessage() != null ? e.getMessage() : "Failed to load restaurants"
                ));
    }

    public void filterRestaurants(String location, Timestamp dateTime, Number guests, @NonNull OnRestaurantsLoadListener listener){

    }


    public interface OnRestaurantsLoadListener {
        void onSuccess(List<Restaurant> restaurants);
        void onFailure(String error);
    }




}
