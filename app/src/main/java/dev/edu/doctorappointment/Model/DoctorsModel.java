package dev.edu.doctorappointment.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoctorsModel implements Serializable {
    private String doctorId;
    private String name;
    private String gender;
    private String profilePicture;
    private String clinicName;
    private List<String> services = new ArrayList<>();
    private List<String> workingHours = new ArrayList<>();
    private String description;
    private int birthYear;
    private boolean isFavorite;
    private int maxBookingsPerSlot;
    private Map<String, Map<String, Integer>> bookingCountByDateTime = new HashMap<>();

    // Constructors, getters, and setters...

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public List<String> getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(List<String> workingHours) {
        this.workingHours = workingHours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public int getMaxBookingsPerSlot() {
        return maxBookingsPerSlot;
    }

    public void setMaxBookingsPerSlot(int maxBookingsPerSlot) {
        this.maxBookingsPerSlot = maxBookingsPerSlot;
    }

    public Map<String, Map<String, Integer>> getBookingCountByDateTime() {
        return bookingCountByDateTime;
    }

    public void setBookingCountByDateTime(Map<String, Map<String, Integer>> bookingCountByDateTime) {
        this.bookingCountByDateTime = bookingCountByDateTime;
    }
}
