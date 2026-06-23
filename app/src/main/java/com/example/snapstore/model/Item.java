package com.example.snapstore.model;

import android.net.Uri;

public class Item {
        private String title;
        private String category;
        private String prize;
        private String rating;

    private boolean isFavorite = false;

    private String image;

        //  Empty constructor (required by Firestore)
        public Item() {
        }

        //  Constructor
        public Item(String title, String category, String price, String rating,String uriImage) {
            this.title = title;
            this.category = category;
            this.prize = price;
            this.rating = rating;
            this.image = uriImage;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getPrize() {
            return prize;
        }

        public void setPrice(String price) {
            this.prize = price;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getImage()
        {
            return image;
        }

    public boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(boolean isFavorite) { this.isFavorite = isFavorite; }

    }


