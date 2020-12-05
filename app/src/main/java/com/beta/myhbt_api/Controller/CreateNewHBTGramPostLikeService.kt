package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface CreateNewHBTGramPostLikeService {
    @POST("/api/v1/hbtGramPostLike/checkLikeStatusAndCreateLike")
    fun createNewHBTGramPostLike(@Query("whoLike") whoLike: String, @Query("postId") postId: String): Call<Any>
}