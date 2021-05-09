package com.beta.cuckoo.Network.Messages

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewMessageRoomService {
    @POST("/api/v1/messageRoom")
    @FormUrlEncoded
    fun createNewMessageRoom(@Field("user1") user1: String, @Field("user2") user2: String): Call<Any>
}