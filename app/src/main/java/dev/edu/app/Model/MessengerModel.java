package dev.edu.app.Model;

public class MessengerModel {
    private String idMessage;
    private String idSender;
    private String idReceiver;
    private String message;
    private String timestamp;
    private String type;

    public MessengerModel() {
    }

    public MessengerModel(String idMessage, String idSender, String idReceiver, String message, String timestamp, String type) {
        this.idMessage = idMessage;
        this.idSender = idSender;
        this.idReceiver = idReceiver;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(String idMessage) {
        this.idMessage = idMessage;
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    public String getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(String idReceiver) {
        this.idReceiver = idReceiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
