package com.beta.myhbt_api.Controller.User

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchUserService {
    @GET("/api/v1/users/searchUser")
    fun searchUser(@Query("fullName") fullName: String): Call<Any>
}