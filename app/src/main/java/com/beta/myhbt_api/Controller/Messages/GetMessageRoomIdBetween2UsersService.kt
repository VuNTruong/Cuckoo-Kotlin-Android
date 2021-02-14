package com.beta.myhbt_api.Controller.Messages

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetMessageRoomIdBetween2UsersService {
    @GET("/api/v1/messageRoom/getMessageRoomIdBetween2Users")
    fun getMessageRoomIddBetween2Users(@Query("user1") user1: String, @Query("user2") user2: String): Call<Any>
}