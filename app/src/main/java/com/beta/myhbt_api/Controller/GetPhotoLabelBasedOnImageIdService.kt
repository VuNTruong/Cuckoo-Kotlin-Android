package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPhotoLabelBasedOnImageIdService {
    @GET("/api/v1/hbtGramPostPhotoLabel")
    fun getPhotoLabels(@Query("imageID") imageID: String): Call<Any>
}