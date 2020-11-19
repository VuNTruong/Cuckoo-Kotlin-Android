package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Query

interface DeleteFollowService {
    @DELETE("/api/v1/hbtGramFollow/deleteHBTGramFollowBetween2Users")
    fun deleteFollow(@Query("follower") follower: String, @Query("following") following: String): Call<Any>
}