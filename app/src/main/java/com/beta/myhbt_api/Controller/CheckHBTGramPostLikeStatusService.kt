package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface CheckHBTGramPostLikeStatusService {
    @POST("/api/v1/hbtGramPostLike/checkLikeStatus")
    fun checkHBTGramPostLikeStatus(@Query("whoLike") whoLike: String, @Query("postId") postId: String): Call<Any>
}