package com.example.dontjusteat.models;

public class MenuItem {
    private String itemId;
    private String itemName;
    private String itemDes;

    private double price;
    private String imageURL;

    public MenuItem() {}


    public MenuItem(String itemId, String itemName, String itemDes, double price, String imageURL) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDes = itemDes;
        this.price = price;
        this.imageURL = imageURL;
    }



    // getters
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getItemDes() { return itemDes; }
    public double getPrice() { return price; }
    public String getImageURL() { return imageURL; }



    // setters
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setItemDes(String itemDes) { this.itemDes = itemDes; }
    public void setPrice(double price) { this.price = price; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
}

