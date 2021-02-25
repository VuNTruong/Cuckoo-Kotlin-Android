package com.beta.cuckoo.Network.Messages

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewChatMessagePhotoService {
    @POST("/api/v1/messagePhoto")
    @FormUrlEncoded
    fun createMessagePhoto(@Field("messageId") messageId: String, @Field("imageURL") imageURL: String): Call<Any>
}