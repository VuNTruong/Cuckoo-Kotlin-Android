package com.beta.cuckoo.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class User (_id: String, firebaseUID: String, fullName: String, email: String, avatarURL: String, coverURL: String, description: String) : Serializable{
    // Attribute of the user as saved in the database
    @SerializedName("_id")
    private val _id: String = _id

    @SerializedName("fullName")
    private val fullName: String = fullName

    @SerializedName("email")
    private val email: String = email

    @SerializedName("avatarURL")
    private val avatarURL: String = avatarURL

    @SerializedName("coverURL")
    private val coverURL: String = coverURL

    @SerializedName("description")
    private val description: String = description

    // Getters
    fun getId(): String {
        return _id
    }

    fun getFullName(): String {
        return fullName
    }

    fun getEmail(): String {
        return email
    }

    fun getAvatarURL(): String {
        return avatarURL
    }

    fun getCoverURL(): String {
        return coverURL
    }

    fun getBio(): String {
        return description
    }
}