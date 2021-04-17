package com.beta.cuckoo.Network.Messages

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetMessagePhotosOfChatRoomService {
    @GET("/api/v1/messagePhoto/getMessagePhotosOfChatRoom")
    fun getMessagePhotosOfChatRoom(@Query("chatRoomId") chatRoomId: String): Call<Any>
}