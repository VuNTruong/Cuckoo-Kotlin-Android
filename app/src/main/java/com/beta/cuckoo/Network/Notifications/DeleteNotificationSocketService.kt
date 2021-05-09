package com.beta.cuckoo.Network.Notifications

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Query

interface DeleteNotificationSocketService {
    @DELETE("/api/v1/cuckooNotificationSocket/deleteNotificationSocket")
    fun deleteNotificationSocket(@Query("userId") userId: String, @Query("socketId") socketId: String, @Query("deviceModel") deviceModel: String): Call<Any>
}