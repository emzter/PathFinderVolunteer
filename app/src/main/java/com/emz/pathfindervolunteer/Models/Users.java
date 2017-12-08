package com.emz.pathfindervolunteer.Models;

/**
 * Created by im_ae on 8/12/2560.
 */

public class Users {
    private int id;
    private String guid;
    private String email;
    private int group;
    private int status;
    private int validate;
    private String proPic;
    private String firstName;
    private String lastName;
    private int sex;
    private String birthdate;
    private int telephone;
    private int category;

    public Users(int id, String guid, String email, int group, int status, int validate, String proPic, String firstName, String lastName, int sex, String birthdate, int telephone, int category) {
        this.id = id;
        this.guid = guid;
        this.email = email;
        this.group = group;
        this.status = status;
        this.validate = validate;
        this.proPic = proPic;
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthdate = birthdate;
        this.telephone = telephone;
        this.category = category;
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

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
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

    public String getProPic() {
        return proPic;
    }

    public void setProPic(String proPic) {
        this.proPic = proPic;
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

    public int getTelephone() {
        return telephone;
    }

    public void setTelephone(int telephone) {
    this.telephone = telephone;
}

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
