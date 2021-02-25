package com.beta.myhbt_api.Model

import com.google.gson.annotations.SerializedName

class PostPhotoLabel (_id: String, imageID: String, imageLabel: String, orderInCollection: Int) {
    // Attributes of the photo label
    @SerializedName("_id")
    private val _id = _id

    @SerializedName("imageID")
    private val imageID = imageID

    @SerializedName("imageLabel")
    private val imageLabel = imageLabel

    @SerializedName("orderInCollection")
    private val orderInCollection = orderInCollection

    // Getters
    fun getId (): String {
        return _id
    }

    fun getImageID (): String {
        return imageID
    }

    fun getImageLabel (): String {
        return imageLabel
    }

    fun getOrderInCollection (): Int {
        return orderInCollection
    }
}