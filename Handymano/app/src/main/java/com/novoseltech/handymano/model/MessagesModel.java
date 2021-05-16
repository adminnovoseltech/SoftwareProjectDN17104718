package com.novoseltech.handymano.model;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class MessagesModel.java
 **/

public class MessagesModel {
    String messageReceiver;
    String lastMessage;

    public MessagesModel() {
    }

    public MessagesModel(String messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    public String getMessageReceiver() {
        return messageReceiver;
    }
}
