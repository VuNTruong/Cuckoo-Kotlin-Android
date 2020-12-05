package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewHBTGramPostCommentPhotoService {
    @POST("/api/v1/hbtGramPostCommentPhoto")
    @FormUrlEncoded
    fun createNewHBTGramPostCommentPhoto(@Field("commentId") commentId: String, @Field("imageURL") imageURL: String): Call<Any>
}