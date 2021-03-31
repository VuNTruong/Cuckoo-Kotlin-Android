package com.beta.cuckoo.Network.Notifications

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface SendDataNotificationToAUser {
    @POST("/api/v1/cuckooNotifications/sendDataNotificationToUserBasedOnUserId")
    fun sendNotificationToAUser(@Query("userId") userId: String,
                                @Query("notificationContent") notificationContent: String, @Query("notificationTitle") notificationTitle: String): Call<Any>
}