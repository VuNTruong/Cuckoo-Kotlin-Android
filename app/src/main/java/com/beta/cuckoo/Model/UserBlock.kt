package com.beta.cuckoo.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class UserBlock (user: String, blockedBy: String, blockType: String) : Serializable {
    @SerializedName("user")
    private val user: String = user

    @SerializedName("blockedBy")
    private val blockedBy: String = blockedBy

    @SerializedName("blockType")
    private val blockType: String = blockType

    // Getters
    fun getBlockedUser () : String {
        return user
    }

    fun getUserBlock () : String {
        return blockedBy
    }

    fun getBlockType () : String {
        return blockType
    }
}