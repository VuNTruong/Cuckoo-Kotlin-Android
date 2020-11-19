package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetFollowStatusService {
    @GET("/api/v1/hbtGramFollow/checkFollowStatus")
    fun getFollowStatus(@Query("follower") follower: String, @Query("following") following: String): Call<Any>
}