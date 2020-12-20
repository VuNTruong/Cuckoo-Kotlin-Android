package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewHBTGramPostPhotoLabelService {
    @POST("/api/v1/hbtGramPostPhotoLabel")
    @FormUrlEncoded
    fun createNewHBTGramPostPhotoLabel(@Field("imageID") imageID: String, @Field("imageLabel") imageLabel: String): Call<Any>
}