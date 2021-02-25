package com.beta.myhbt_api.Network.LikesAndComments

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPostCommentPhotoService {
    @GET("/api/v1/cuckooPostCommentPhoto")
    fun getPostCommentPhoto(@Query("commentId") commentId: String): Call<Any>
}