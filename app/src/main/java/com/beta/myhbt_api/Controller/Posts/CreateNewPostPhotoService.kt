package com.beta.myhbt_api.Controller.Posts

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewPostPhotoService {
    @POST("/api/v1/cuckooPostPhoto")
    @FormUrlEncoded
    fun createNewHBTGramPostPhoto(@Field("postId") postId: String, @Field("imageURL") imageURL: String): Call<Any>
}