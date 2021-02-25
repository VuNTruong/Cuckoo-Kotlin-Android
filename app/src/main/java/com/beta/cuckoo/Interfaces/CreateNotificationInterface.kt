package com.beta.cuckoo.Interfaces

interface CreateNotificationInterface {
    fun createNotification (content: String, forUser: String, fromUser: String, image: String, postId: String)
}