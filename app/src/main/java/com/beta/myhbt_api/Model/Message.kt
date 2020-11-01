package com.beta.myhbt_api.Model

class Message (sender: String, receiver: String, content: String, messageId: String) {
    // Info of the message
    private val messageId = messageId
    private val sender = sender
    private val receiver = receiver
    private val content = content

    // Getters
    fun getSender() : String {
        return sender
    }

    fun getReceiver(): String {
        return receiver
    }

    fun getContent(): String {
        return content
    }

    fun getMessageId(): String {
        return messageId
    }
}