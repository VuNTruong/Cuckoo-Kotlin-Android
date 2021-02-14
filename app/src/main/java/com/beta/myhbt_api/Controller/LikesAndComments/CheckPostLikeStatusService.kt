package com.beta.myhbt_api.Controller.LikesAndComments

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface CheckPostLikeStatusService {
    @POST("/api/v1/cuckooPostLike/checkLikeStatus")
    fun checkHBTGramPostLikeStatus(@Query("whoLike") whoLike: String, @Query("postId") postId: String): Call<Any>
}