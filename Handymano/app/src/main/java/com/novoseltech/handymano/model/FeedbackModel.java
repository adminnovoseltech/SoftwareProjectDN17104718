package com.novoseltech.handymano.model;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class FeedbackModel.java
 **/

public class FeedbackModel {
    String username;
    String feedback_text;
    String creation_date;
    String user_id;
    long stars;

    public FeedbackModel() {
    }

    public FeedbackModel(String username, String feedback_text, String creation_date, String user_id, long stars) {
        this.username = username;
        this.feedback_text = feedback_text;
        this.creation_date = creation_date;
        this.user_id = user_id;
        this.stars = stars;
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

    public String getUser_id() {
        return user_id;
    }

    public long getStars() {
        return stars;
    }
}
