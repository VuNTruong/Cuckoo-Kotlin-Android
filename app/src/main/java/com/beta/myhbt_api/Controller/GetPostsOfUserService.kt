package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPostsOfUserService {
    @GET("/api/v1/hbtGramPost")
    fun getPostsOfUser(@Query("writer") userId: String): Call<Any>
}