package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetHBTGramPostDetail {
    @GET("/api/v1/hbtGramPost/getHBTGramPostDetail")
    fun getPostDetail(@Query("postId") postId: String): Call<Any>
}