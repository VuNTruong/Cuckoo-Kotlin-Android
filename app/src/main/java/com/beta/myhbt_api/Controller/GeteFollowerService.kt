package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeteFollowerService {
    @GET("/api/v1/hbtGramFollow")
    fun getFollowers(@Query("following") userId: String): Call<Any>
}