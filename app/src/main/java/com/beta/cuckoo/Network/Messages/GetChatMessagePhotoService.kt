package com.beta.cuckoo.Network.Messages

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetChatMessagePhotoService {
    @GET("/api/v1/messagePhoto")
    fun getMessagePhoto(@Query("messageId") messageId: String): Call<Any>
}