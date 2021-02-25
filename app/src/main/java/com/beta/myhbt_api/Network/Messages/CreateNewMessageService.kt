package com.beta.myhbt_api.Network.Messages

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewMessageService {
    @POST("/api/v1/message")
    @FormUrlEncoded
    fun createNewMessage(@Field("sender") sender: String, @Field("receiver") receiver: String,
                         @Field("content") content: String): Call<Any>
}