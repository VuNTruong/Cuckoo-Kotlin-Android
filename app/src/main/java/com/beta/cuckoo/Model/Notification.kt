package com.beta.cuckoo.Model

import com.google.gson.annotations.SerializedName

class Notification (_id: String, fromUser: String, forUser: String, postId: String, image: String, content: String, orderInCollection: Int) {
    // Attributes of the notification
    @SerializedName("_id")
    private val _id: String = _id

    @SerializedName("fromUser")
    private val fromUser: String = fromUser

    @SerializedName("forUser")
    private val forUser: String = forUser

    @SerializedName("postId")
    private val postId = postId

    @SerializedName("image")
    private val image = image

    @SerializedName("content")
    private val content = content

    @SerializedName("orderInCollection")
    private val orderInCollection = orderInCollection

    // Getters
    fun getId (): String {
        return _id
    }

    fun getFromUser (): String {
        return fromUser
    }

    fun getForUser (): String {
        return forUser
    }

    fun getPostId (): String {
        return postId
    }

    fun getImage (): String {
        return image
    }

    fun getContent (): String {
        return content
    }

    fun getOrderInCollection (): Int {
        return orderInCollection
    }
}