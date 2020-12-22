package com.beta.myhbt_api.Model

import com.google.gson.annotations.SerializedName

class UserLikeInteraction (user: String, likedBy: String, numOfLikes: Int) {
    // Info of user like interaction
    @SerializedName("user")
    val user = user

    @SerializedName("likedBy")
    val likedBy = likedBy

    @SerializedName("numOfLikes")
    val numOfLikes = numOfLikes

    // Getters
    fun getLikedUser () : String {
        return user
    }

    fun getUserLiked () : String {
        return likedBy
    }

    fun getUserNumOfLikes () : Int {
        return numOfLikes
    }
}