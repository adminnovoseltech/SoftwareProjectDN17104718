package com.novoseltech.handymano.model;

import java.util.Date;

public class ChatModel {

    String user_id, sender, message;
    Date timestamp;

    public ChatModel(String user_id, String sender, String message, Date timestamp) {
        this.user_id = user_id;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ChatModel() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
