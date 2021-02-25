package com.beta.cuckoo.Model

import com.google.gson.annotations.SerializedName

class UserCommentInteraction (user: String, commentedBy: String, numOfComments: Int) {
    // Info of user comment interaction
    @SerializedName("user")
    val user = user

    @SerializedName("commentedBy")
    val commentedBy = commentedBy

    @SerializedName("numOfComments")
    val numOfComments = numOfComments

    // Getters
    fun getCommentedUser () : String {
        return user
    }

    fun getUserCommented () : String {
        return commentedBy
    }

    fun getUserNumOfComments () : Int {
        return numOfComments
    }
}