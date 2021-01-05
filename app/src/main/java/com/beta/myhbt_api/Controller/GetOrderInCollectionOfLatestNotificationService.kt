package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET

interface GetOrderInCollectionOfLatestNotificationService {
    @GET("/api/v1/hbtGramNotifications/getOrderInCollectionOfLatestNotification")
    fun getOrderInCollectionOfLatestNotification(): Call<Any>
}