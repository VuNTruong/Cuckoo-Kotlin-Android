package com.beta.myhbt_api.Interfaces

interface CreateNotificationInterface {
    fun createNotification (content: String, forUser: String, fromUser: String, image: String, postId: String)
}