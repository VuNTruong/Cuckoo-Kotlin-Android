package com.beta.cuckoo.Network.VideoChat

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface CreateVideoChatRoomService {
    @POST("/api/v1/videoChat/createRoom")
    fun createVideoChatRoom(@Query("chatRoomName") roomName: String): Call<Any>
}