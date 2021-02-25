package com.beta.cuckoo.Network.LikesAndComments

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPostCommentsService {
    @GET("/api/v1/cuckooPostComment")
    fun getPostComments(@Query("postId") postId: String): Call<Any>
}