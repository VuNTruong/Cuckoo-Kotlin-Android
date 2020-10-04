package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET

interface GetAllClassGroupChatMessagesService {
    @GET("/api/v1/classGroupChat")
    fun getAllMessages(): Call<Any>
}