package com.beta.cuckoo.Model

import com.google.gson.annotations.SerializedName

class UserInteraction (user: String, interactWith: String, interactionFrequency: Int) {
    // Info of user interaction
    @SerializedName("user")
    val user = user

    @SerializedName("interactWith")
    val interactWith = interactWith

    @SerializedName("interactionFrequency")
    val interactionFrequency = interactionFrequency

    // Getters
    fun getInteractedUser () : String {
        return user
    }

    fun getUserInteractWith () : String {
        return interactWith
    }

    fun getUserInteractionFrequency () : Int {
        return interactionFrequency
    }
}