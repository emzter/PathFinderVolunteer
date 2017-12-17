package com.emz.pathfindervolunteer.Models;

import com.google.gson.annotations.SerializedName;

public class Orders {
    @SerializedName("id")
    private int id;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("category")
    private int category;
    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("status")
    private int status;
    @SerializedName("volunteer_id")
    private int volunteerId;
    private boolean read;

    public Orders(int id, int userId, int category, double lat, double lng, String createdAt, int status, int volunteerId) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
        this.status = status;
        this.volunteerId = volunteerId;
    }

    public Orders(int id, int userId, int category, double lat, double lng, String createdAt, int status, int volunteerId, boolean read) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.lat = lat;
        this.lng = lng;
        this.createdAt = createdAt;
        this.status = status;
        this.volunteerId = volunteerId;
        this.read = read;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(int volunteerId) {
        this.volunteerId = volunteerId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
