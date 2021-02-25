package com.beta.myhbt_api.Network.LikesAndComments

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewPostCommentService {
    @POST("/api/v1/cuckooPostComment")
    @FormUrlEncoded
    fun createNewHBTGramPostComment(@Field("content") content: String, @Field("writer") writer: String, @Field("postId") postId: String): Call<Any>
}