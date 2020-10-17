package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewHBTGramPostLikeService {
    @POST("/api/v1/hbtGramPostLike")
    @FormUrlEncoded
    fun createNewHBTGramPostLike(@Field("whoLike") whoLike: String, @Field("postId") postId: String): Call<Any>
}