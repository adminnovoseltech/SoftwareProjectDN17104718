package com.novoseltech.handymano.model;

public class FeedbackModel {
    String username;
    String feedback_text;
    String creation_date;

    public FeedbackModel() {
    }

    public FeedbackModel(String username, String feedback_text, String creation_date) {
        this.username = username;
        this.feedback_text = feedback_text;
        this.creation_date = creation_date;
    }

    public String getUsername() {
        return username;
    }

    public String getFeedback_text() {
        return feedback_text;
    }

    public String getCreation_date() {
        return creation_date;
    }
}
