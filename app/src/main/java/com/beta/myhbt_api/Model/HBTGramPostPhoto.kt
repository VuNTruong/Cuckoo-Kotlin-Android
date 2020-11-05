package com.beta.myhbt_api.Model

import com.google.gson.annotations.SerializedName

class HBTGramPostPhoto (imageURL: String) {
    // Info of the image
    @SerializedName("imageURL")
    private val imageURL = imageURL

    // Getter
    fun getImageURL() : String {
        return imageURL
    }
}