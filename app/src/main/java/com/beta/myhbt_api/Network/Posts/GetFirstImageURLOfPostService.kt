package com.beta.myhbt_api.Network.Posts

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetFirstImageURLOfPostService {
    @GET("/api/v1/cuckooPostPhoto")
    fun getFirstPhotoURL(@Query("postId") postId: String): Call<Any>
}