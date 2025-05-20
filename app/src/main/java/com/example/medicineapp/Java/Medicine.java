package com.example.medicineapp.Java;

import java.io.Serializable;

public class Medicine implements Serializable
{
    private String id;
    private String name ;
    private String expiration;
    private String mealtime;
    private String quantity;
    private String photo;

    public Medicine() {
    }

    public Medicine(String name, String expiration, String mealtime, String quantity, String photo) {
        this.name = name;
        this.expiration = expiration;
        this.mealtime = mealtime;
        this.quantity = quantity;
        this.photo = photo;
    }

    // Getter and Setter for ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public String getMealtime() {
        return mealtime;
    }

    public void setMealtime(String mealtime) {
        this.mealtime = mealtime;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public String toString() {
        return "Medicine{" +
                "name='" + name + '\'' +
                ", expiration='" + expiration + '\'' +
                ", mealtime='" + mealtime + '\'' +
                ", quantity='" + quantity + '\'' +
                ", photo='" + photo + '\'' +
                '}';
    }
}
