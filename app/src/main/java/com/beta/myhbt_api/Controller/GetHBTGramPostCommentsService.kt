package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetHBTGramPostCommentsService {
    @GET("/api/v1/hbtGramPostComment")
    fun getPostComments(@Query("postId") postId: String): Call<Any>
}