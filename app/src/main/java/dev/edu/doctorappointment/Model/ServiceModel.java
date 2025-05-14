package dev.edu.doctorappointment.Model;

public class ServiceModel {

    public String name;
    public String keyID;
    public String price;
    public String icon;
    public String description;

    public ServiceModel() {
    }

    public ServiceModel(String name, String keyID, String price, String icon, String description) {
        this.name = name;
        this.keyID = keyID;
        this.price = price;
        this.icon = icon;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeyID() {
        return keyID;
    }

    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
