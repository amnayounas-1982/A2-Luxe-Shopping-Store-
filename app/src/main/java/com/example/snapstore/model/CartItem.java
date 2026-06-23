package com.example.snapstore.model;

public class CartItem {
    private String title;
    private String price;
    private String rating;
    private String image;
    private int quantity;
    public boolean isFavorite;
    private String id;
    private String orderId;




    public CartItem() {} // Needed for Firebase

    public CartItem(String title, String price, String rating, String image, int quantity,String id) {
        this.title = title;
        this.price = price;
        this.rating = rating;
        this.image = image;
        this.quantity = quantity;
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = price;
    }
    public String getRating() {
        return rating;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

}
