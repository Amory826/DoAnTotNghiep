package dev.edu.app.Model;

public class FavouModel {
    private String keyID;
    private String idUser;
    private DoctorsModel doctorsModel;

    public FavouModel() {

    }

    public FavouModel(String keyID, String idUser, DoctorsModel doctorsModel) {
        this.keyID = keyID;
        this.idUser = idUser;
        this.doctorsModel = doctorsModel;
    }

    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public DoctorsModel getDoctorsModel() {
        return doctorsModel;
    }

    public void setDoctorsModel(DoctorsModel doctorsModel) {
        this.doctorsModel = doctorsModel;
    }
}
