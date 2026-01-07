package com.example.dontjusteat.models;

public class Table {
    private String id;
    private int capacity;
    private boolean active;

    
    public Table() {
    }



    public Table(String id, int capacity, boolean active) {
        this.id = id;
        this.capacity = capacity;
        this.active = active;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Display name: use table ID as identifier
    public String getDisplayName() {
        return "Table " + id;
    }
}

