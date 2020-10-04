package com.beta.myhbt_api.Model

import com.google.gson.annotations.SerializedName

class User (_id: String, firstName: String, middleName: String, lastName: String, email: String, role: String,
            classCode: String, avatarURL: String, coverURL: String, passwordChangedAt: String, studentId: String){
    // Attribute of the user as saved in the database
    @SerializedName("_id")
    private val _id: String = _id

    @SerializedName("firstName")
    private val firstName: String = firstName

    @SerializedName("middleName")
    private val middleName: String = middleName

    @SerializedName("lastName")
    private val lastName: String = lastName

    @SerializedName("email")
    private val email: String = email

    @SerializedName("role")
    private val role: String = role

    @SerializedName("studentId")
    private val studentId: String = studentId

    @SerializedName("classCode")
    private val classCode: String = classCode

    @SerializedName("avatarURL")
    private val avatarURL: String = avatarURL

    @SerializedName("coverURL")
    private val coverURL: String = coverURL

    @SerializedName("passwordChangedAt")
    private val passwordChangedAt: String = passwordChangedAt

    // Getters
    fun getId(): String {
        return _id
    }

    fun getFullName(): String {
        return "$lastName $middleName $firstName"
    }

    fun getEmail(): String {
        return email
    }

    fun getRole(): String {
        return role
    }

    fun getStudentId(): String {
        return studentId
    }

    fun getClassCode(): String {
        return classCode
    }

    fun getAvatarURL(): String {
        return avatarURL
    }

    fun getCoverURL(): String {
        return coverURL
    }

    fun getPasswordChangeAt(): String {
        return passwordChangedAt
    }
}