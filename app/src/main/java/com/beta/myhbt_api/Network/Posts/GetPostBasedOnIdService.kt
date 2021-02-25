package com.beta.myhbt_api.Network.Posts

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPostBasedOnIdService {
    @GET("/api/v1/cuckooPost")
    fun getPostBasedOnId(@Query("_id") postId: String): Call<Any>
}