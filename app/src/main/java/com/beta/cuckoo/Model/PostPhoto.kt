package com.beta.cuckoo.Model

import com.google.gson.annotations.SerializedName

class PostPhoto (imageURL: String, postId: String) {
    // Info of the image
    @SerializedName("imageURL")
    private val imageURL = imageURL

    @SerializedName("postId")
    private val postId = postId

    // Getter
    fun getImageURL() : String {
        return imageURL
    }

    fun getPhotoId() : String {
        return postId
    }
}