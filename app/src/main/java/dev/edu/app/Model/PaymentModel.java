package dev.edu.app.Model;

public class PaymentModel {
    private String paymentId;
    private String userId;
    private String appointmentId;
    private String amount;
    private String status;
    private String timestamp;

    // Constructors
    public PaymentModel() {}

    public PaymentModel(String paymentId, String userId, String appointmentId, String amount, String status, String timestamp) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
