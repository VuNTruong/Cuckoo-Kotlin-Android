package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPostsWithinARadiusService {
    @GET("/api/v1/hbtGramPost/getHBTGramPostWithinRadius")
    fun getPostsWithinARadius(@Query("location") location: String, @Query("radius") radius: Int, @Query("currentLocationInList") currentLocationInList: Int): Call<Any>
}