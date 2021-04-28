package com.novoseltech.handymano.model;

public class ServicesModel {

    private String username;
    private String user_id;
    private String category;
    private double distance;

    public ServicesModel() {
    }

    public ServicesModel(String username, String user_id, String category, double distance) {
        this.username = username;
        this.user_id = user_id;
        this.category = category;
        this.distance = distance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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
