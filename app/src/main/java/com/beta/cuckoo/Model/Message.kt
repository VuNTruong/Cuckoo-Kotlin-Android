package com.beta.cuckoo.Model

import com.google.gson.annotations.SerializedName

class Message (sender: String, receiver: String, content: String, _id: String) {
    // Info of the message
    @SerializedName("_id")
    private val _id = _id

    @SerializedName("sender")
    private val sender = sender

    @SerializedName("receiver")
    private val receiver = receiver

    @SerializedName("content")
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
        return _id
    }
}