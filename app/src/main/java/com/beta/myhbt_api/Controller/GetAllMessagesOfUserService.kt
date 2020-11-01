package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetAllMessagesOfUserService {
    @GET("/api/v1/message/queryWithOrCondition")
    fun getAllMessages(@Query("sender") currentUserId: String, @Query("receiver") alsoCurrentUserId: String): Call<Any>
}