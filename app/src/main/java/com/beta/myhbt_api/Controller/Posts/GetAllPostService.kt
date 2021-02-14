package com.beta.myhbt_api.Controller.Posts

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetAllPostService {
    @GET("/api/v1/cuckooPost/getCuckooPostForUser")
    fun getAllPosts(@Query("userId") userId: String, @Query("currentLocationInList") currentLocationInList: Int): Call<Any>
}