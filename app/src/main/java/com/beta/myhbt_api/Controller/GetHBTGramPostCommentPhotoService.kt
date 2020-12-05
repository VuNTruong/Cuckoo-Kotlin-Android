package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetHBTGramPostCommentPhotoService {
    @GET("/api/v1/hbtGramPostCommentPhoto")
    fun getPostCommentPhoto(@Query("commentId") commentId: String): Call<Any>
}