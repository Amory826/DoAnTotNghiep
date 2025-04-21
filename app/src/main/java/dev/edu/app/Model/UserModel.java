package dev.edu.app.Model;

import java.util.ArrayList;
import java.util.List;

public class UserModel {
    private String keyID;
    private String name;
    private String email;
    private String password;
    private String phone;
    private List<DoctorsModel> favoriteDoctors = new ArrayList<>();

    public UserModel() {
    }

    public UserModel(String keyID, String name, String email, String password, String phone) {
        this.keyID = keyID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public UserModel(String keyID, String name, String email, String password, String phone, List<DoctorsModel> favoriteDoctors) {
        this.keyID = keyID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.favoriteDoctors = favoriteDoctors;
    }

    public List<DoctorsModel> getFavoriteDoctors() {
        return favoriteDoctors;
    }

    public void setFavoriteDoctors(List<DoctorsModel> favoriteDoctors) {
        this.favoriteDoctors = favoriteDoctors;
    }

    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
