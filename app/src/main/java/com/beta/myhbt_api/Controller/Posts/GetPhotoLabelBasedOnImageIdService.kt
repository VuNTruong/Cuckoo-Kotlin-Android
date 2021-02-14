package com.beta.myhbt_api.Controller.Posts

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPhotoLabelBasedOnImageIdService {
    @GET("/api/v1/cuckooPostPhotoLabel")
    fun getPhotoLabels(@Query("imageID") imageID: String): Call<Any>
}