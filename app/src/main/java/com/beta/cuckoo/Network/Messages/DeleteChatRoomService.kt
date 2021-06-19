package com.beta.cuckoo.Network.Messages

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Query

interface DeleteChatRoomService {
    @DELETE("/api/v1/messageRoom")
    fun deleteChatRoom(@Query("messageRoomId") messageRoomId: String): Call<Any>
}