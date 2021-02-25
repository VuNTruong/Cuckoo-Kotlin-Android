package com.beta.myhbt_api.Network.User

import retrofit2.Call
import retrofit2.http.POST

interface LogoutPostDataService {
    @POST("/api/v1/users/logout")
    fun logout(): Call<Any>
}