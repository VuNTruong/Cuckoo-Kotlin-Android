package com.beta.myhbt_api.Network.Messages

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetAllMessagesOfChatRoomService {
    @GET("/api/v1/message")
    fun getAllMessagesOfChatRoom(@Query("chatRoomId") chatRoomId: String): Call<Any>
}