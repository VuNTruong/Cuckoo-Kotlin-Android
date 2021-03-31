package com.beta.cuckoo.Network.VideoChat

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Query

interface DeleteVideoChatRoomService {
    @DELETE("/api/v1/videoChat/endRoom")
    fun deleteVideoChatRoom(@Query("chatRoomName") roomName: String): Call<Any>
}