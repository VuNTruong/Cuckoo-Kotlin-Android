package com.beta.cuckoo.Model

import com.google.gson.annotations.SerializedName

class UserProfileVisit (user: String, visitedBy: String, numOfVisits: Int) {
    // Info of user profile visit interaction
    @SerializedName("user")
    val user = user

    @SerializedName("visitedBy")
    val visitedBy = visitedBy

    @SerializedName("numOfVisits")
    val numOfVisits = numOfVisits

    // Getters
    fun getVisitedUser () : String {
        return user
    }

    fun getUserVisited () : String {
        return visitedBy
    }

    fun getUserNumOfVisits () : Int {
        return numOfVisits
    }
}