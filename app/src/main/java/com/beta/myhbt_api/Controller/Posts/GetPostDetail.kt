package com.beta.myhbt_api.Controller.Posts

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPostDetail {
    @GET("/api/v1/cuckooPost/getCuckooPostDetail")
    fun getPostDetail(@Query("postId") postId: String): Call<Any>
}