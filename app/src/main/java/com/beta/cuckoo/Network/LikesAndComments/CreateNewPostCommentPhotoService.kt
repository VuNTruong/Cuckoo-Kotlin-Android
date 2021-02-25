package com.beta.cuckoo.Network.LikesAndComments

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewPostCommentPhotoService {
    @POST("/api/v1/cuckooPostCommentPhoto")
    @FormUrlEncoded
    fun createNewHBTGramPostCommentPhoto(@Field("commentId") commentId: String, @Field("imageURL") imageURL: String): Call<Any>
}