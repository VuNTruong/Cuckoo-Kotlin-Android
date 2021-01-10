package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetAllHBTGramPostService {
    @GET("/api/v1/hbtGramPost/getHBTGramPostForUser")
    fun getAllPosts(@Query("userId") userId: String, @Query("currentLocationInList") currentLocationInList: Int): Call<Any>
}