package dev.edu.app.Model;

import java.io.Serializable;

public class AppointmentModel implements Serializable {
    private String appointmentId;
    private String userId;
    private String doctorId;
    private String clinicName;
    private String serviceId;
    private String appointmentSlot;
    private String appointmentTime;
    private String serviceImage;
    private String status;

    // Constructors
    public AppointmentModel() {}

    public AppointmentModel(String appointmentId, String userId, String doctorId, String clinicName, String serviceId, String appointmentSlot, String appointmentTime, String serviceImage, String status) {
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.doctorId = doctorId;
        this.clinicName = clinicName;
        this.serviceId = serviceId;
        this.appointmentSlot = appointmentSlot;
        this.appointmentTime = appointmentTime;
        this.serviceImage = serviceImage;
        this.status = status;
    }

    // Getters and Setters
    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getAppointmentSlot() {
        return appointmentSlot;
    }

    public void setAppointmentSlot(String appointmentSlot) {
        this.appointmentSlot = appointmentSlot;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getServiceImage() {
        return serviceImage;
    }

    public void setServiceImage(String serviceImage) {
        this.serviceImage = serviceImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

