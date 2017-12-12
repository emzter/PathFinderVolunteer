package com.emz.pathfindervolunteer.Models;

import com.google.gson.annotations.SerializedName;

public class Users {
    @SerializedName("id")
    private int id;
    @SerializedName("email")
    private String email;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("profile_picture")
    private String proPic;
    @SerializedName("telephone")
    private String telephone;
    @SerializedName("car_type")
    private int carType;
    @SerializedName("category")
    private String category;
    @SerializedName("status")
    private int status;
    @SerializedName("validate")
    private int validate;
    @SerializedName("online")
    private int online;
    @SerializedName("lat")
    private double lat;
    @SerializedName("lng")
    private double lng;

    public Users(int id, String email, String firstName, String lastName, String proPic, String telephone, int carType, String category, int status, int validate, int online, double lat, double lng) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.proPic = proPic;
        this.telephone = telephone;
        this.carType = carType;
        this.category = category;
        this.status = status;
        this.validate = validate;
        this.online = online;
        this.lat = lat;
        this.lng = lng;
    }

    public Users(int id, String email, String firstName, String lastName, String proPic, String telephone, int carType, String category, int status, int validate, int online) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.proPic = proPic;
        this.telephone = telephone;
        this.carType = carType;
        this.category = category;
        this.status = status;
        this.validate = validate;
        this.online = online;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProPic() {
        return proPic;
    }

    public void setProPic(String proPic) {
        this.proPic = proPic;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getCarType() {
        return carType;
    }

    public void setCarType(int carType) {
        this.carType = carType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getValidate() {
        return validate;
    }

    public void setValidate(int validate) {
        this.validate = validate;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
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
}
