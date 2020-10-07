package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetUserInfoService {
    @GET("/api/v1/users")
    fun getUserInfo(@Query("email") email: String): Call<Any>
}