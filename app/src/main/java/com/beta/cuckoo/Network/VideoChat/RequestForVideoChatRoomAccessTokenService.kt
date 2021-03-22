package com.beta.cuckoo.Network.VideoChat

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RequestForVideoChatRoomAccessTokenService {
    @GET("/api/v1/videoChat/getAccessToken")
    fun requestForVideoChatRoomAccessToken(@Query("userId") userId: String, @Query("roomId") roomName: String): Call<Any>
}