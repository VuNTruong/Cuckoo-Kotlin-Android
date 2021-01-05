package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNotificationService {
    @POST("/api/v1/hbtGramNotifications")
    @FormUrlEncoded
    fun createNewNotification(@Field("content") content: String, @Field("forUser") forUser: String,
                              @Field("fromUser") fromUser: String, @Field("image") image: String, @Field("postId") postId: String): Call<Any>
}