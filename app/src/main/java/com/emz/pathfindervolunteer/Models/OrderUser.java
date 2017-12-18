package com.emz.pathfindervolunteer.Models;

import com.google.gson.annotations.SerializedName;

public class OrderUser {
    @SerializedName("id")
    private int id;
    @SerializedName("guid")
    private String guid;
    @SerializedName("email")
    private String email;
    @SerializedName("user_group")
    private String group;
    @SerializedName("status")
    private String status;
    @SerializedName("validate")
    private String validate;
    @SerializedName("profile_image")
    private String proPic;
    @SerializedName("header_image")
    private String headerPic;
    @SerializedName("first_name")
    private String fname;
    @SerializedName("last_name")
    private String lname;
    @SerializedName("sex")
    private int sex;
    @SerializedName("birthdate")
    private String birthdate;
    @SerializedName("telephone")
    private String telephone;
    @SerializedName("facebook")
    private String facebook;
    @SerializedName("twitter")
    private String twitter;
    @SerializedName("line")
    private String line;
    @SerializedName("other_link")
    private String otherlink;

    public OrderUser(int id, String guid, String email, String group, String status, String validate, String proPic, String headerPic, String fname, String lname, int sex, String birthdate, String telephone, String facebook, String twitter, String line, String otherlink) {
        this.id = id;
        this.guid = guid;
        this.email = email;
        this.group = group;
        this.status = status;
        this.validate = validate;
        this.proPic = proPic;
        this.headerPic = headerPic;
        this.fname = fname;
        this.lname = lname;
        this.sex = sex;
        this.birthdate = birthdate;
        this.telephone = telephone;
        this.facebook = facebook;
        this.twitter = twitter;
        this.line = line;
        this.otherlink = otherlink;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getValidate() {
        return validate;
    }

    public void setValidate(String validate) {
        this.validate = validate;
    }

    public String getProPic() {
        return proPic;
    }

    public void setProPic(String proPic) {
        this.proPic = proPic;
    }

    public String getHeaderPic() {
        return headerPic;
    }

    public void setHeaderPic(String headerPic) {
        this.headerPic = headerPic;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getOtherlink() {
        return otherlink;
    }

    public void setOtherlink(String otherlink) {
        this.otherlink = otherlink;
    }

    public String getFullName() { return fname+" "+lname; }

}
