package com.beta.cuckoo.Network.Notifications

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PATCH

interface UpdateNotificationSocketService {
    @PATCH("/api/v1/cuckooNotificationSocket/updateNotificationSocket")
    @FormUrlEncoded
    fun updateNotificationSocket(@Field("userId") userId: String,
                                 @Field("socketId") socketId: String, @Field("deviceModel") deviceModel: String): Call<Any>
}