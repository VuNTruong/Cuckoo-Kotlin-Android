package com.beta.myhbt_api.Model

import com.google.gson.annotations.SerializedName

class HBTGramPostPhoto (imageURL: String, postId: String) {
    // Info of the image
    @SerializedName("imageURL")
    private val imageURL = imageURL

    @SerializedName("postId")
    private val postId = postId

    // Getter
    fun getImageURL() : String {
        return imageURL
    }

    fun getPostId() : String {
        return postId
    }
}