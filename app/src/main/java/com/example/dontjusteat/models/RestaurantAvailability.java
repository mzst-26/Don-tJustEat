package com.example.dontjusteat.models;

import java.util.List;

public class RestaurantAvailability {
    public final Restaurant restaurant;
    public final List<Slot> slots;

    public RestaurantAvailability(Restaurant restaurant, List<Slot> slots) {
        this.restaurant = restaurant;
        this.slots = slots;
    }
}
