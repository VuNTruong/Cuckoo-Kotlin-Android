package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPhotosOfUserService {
    @GET("/api/v1/hbtGramPostPhoto/getPhotosOfUser")
    fun getPhotosOfUser(@Query("userId") userId: String): Call<Any>
}