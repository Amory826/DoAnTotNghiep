package dev.edu.doctorappointment.Model;

public class FollowUpAppointmentModel {
    private String followUpId;
    private String originalAppointmentId;
    private String userId;
    private String doctorId;
    private String appointmentTime;
    private String appointmentSlot;
    private String serviceId;
    private String status;
    private String notes;

    public FollowUpAppointmentModel() {
        // Required empty constructor for Firebase
    }

    public FollowUpAppointmentModel(String followUpId, String originalAppointmentId, String userId, 
                                  String doctorId, String appointmentTime, String appointmentSlot, 
                                  String serviceId, String status, String notes) {
        this.followUpId = followUpId;
        this.originalAppointmentId = originalAppointmentId;
        this.userId = userId;
        this.doctorId = doctorId;
        this.appointmentTime = appointmentTime;
        this.appointmentSlot = appointmentSlot;
        this.serviceId = serviceId;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public String getFollowUpId() {
        return followUpId;
    }

    public void setFollowUpId(String followUpId) {
        this.followUpId = followUpId;
    }

    public String getOriginalAppointmentId() {
        return originalAppointmentId;
    }

    public void setOriginalAppointmentId(String originalAppointmentId) {
        this.originalAppointmentId = originalAppointmentId;
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

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getAppointmentSlot() {
        return appointmentSlot;
    }

    public void setAppointmentSlot(String appointmentSlot) {
        this.appointmentSlot = appointmentSlot;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
} 