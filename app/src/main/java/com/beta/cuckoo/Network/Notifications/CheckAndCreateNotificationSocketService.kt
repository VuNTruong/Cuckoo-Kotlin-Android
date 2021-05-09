package com.beta.cuckoo.Network.Notifications

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface CheckAndCreateNotificationSocketService {
    @POST("/api/v1/cuckooNotificationSocket/checkAndCreateNotificationSocket")
    fun checkAndCreateNotificationSocket(@Query("userId") userId: String, @Query("socketId") socketId: String): Call<Any>
}