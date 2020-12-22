package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetBriefUserStatsService {
    @GET("/api/v1/hbtGramAccountStats/getBriefAccountStats")
    fun getBriefAccountStats(@Query("userId") userId: String, @Query("limit") limit: Int): Call<Any>
}