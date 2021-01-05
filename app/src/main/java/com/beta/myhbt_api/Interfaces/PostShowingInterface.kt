package com.beta.myhbt_api.Interfaces

interface PostShowingInterface {
    fun createNotification (content: String, forUser: String, fromUser: String, image: String, postId: String)
    fun loadMorePosts ()
}