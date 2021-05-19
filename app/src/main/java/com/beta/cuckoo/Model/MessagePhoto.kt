package com.beta.cuckoo.Model

import com.google.gson.annotations.SerializedName

class MessagePhoto (_id: String, imageURL: String, messageId: String) {
    // Info of the message photo
    @SerializedName("_id")
    private val _id = _id

    @SerializedName("imageURL")
    private val imageURL = imageURL

    @SerializedName("messageId")
    private val messageId = messageId

    // Getters
    fun getId (): String {
        return _id
    }

    fun getImageURL (): String {
        return imageURL
    }

    fun getMessageID (): String {
        return messageId
    }
}