package com.beta.myhbt_api.Controller.Notifications

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetNotificationsForUserService {
    @GET("/api/v1/cuckooNotifications/getNotificationsForUser")
    fun getNotificationsForUser(@Query("userId") userId: String, @Query("currentLocationInList") currentLocationInList: Int): Call<Any>
}