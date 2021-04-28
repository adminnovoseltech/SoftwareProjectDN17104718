package com.novoseltech.handymano.model;

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
