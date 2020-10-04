package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET

interface GetDataService {
    @GET("/api/v1/users/userArray")
    fun getAllUsersMap(): Call<Any>
}