package com.beta.cuckoo.Network.Messages

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetMessageObjectBasedOnIdService {
    @GET("/api/v1/message")
    fun getMessageObjectBasedOnId(@Query("_id") messageId: String): Call<Any>
}