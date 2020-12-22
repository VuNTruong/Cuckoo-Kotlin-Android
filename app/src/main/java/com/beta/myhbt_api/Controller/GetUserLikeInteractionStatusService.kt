package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetUserLikeInteractionStatusService {
    @GET("/api/v1/hbtGramAccountStats/getUserLikeInteractionStatus")
    fun getUserLikeInteractionStatus(@Query("userId") userId: String, @Query("limit") limit: Int): Call<Any>
}