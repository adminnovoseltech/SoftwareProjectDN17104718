package com.novoseltech.handymano.model;

public class ServicesModel {

    private String username;
    private String category;
    private double distance;

    public ServicesModel() {
    }

    public ServicesModel(String username, String category, double distance) {
        this.username = username;
        this.category = category;
        this.distance = distance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getDistance() {
        return distance;
    }
}
