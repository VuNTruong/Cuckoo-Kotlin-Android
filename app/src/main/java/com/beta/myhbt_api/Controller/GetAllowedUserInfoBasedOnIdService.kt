package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetAllowedUserInfoBasedOnIdService {
    @GET("/api/v1/allowedUsers")
    fun getAllowedUserInfoBasedOnId(@Query("studentId") studentId: String): Call<Any>
}