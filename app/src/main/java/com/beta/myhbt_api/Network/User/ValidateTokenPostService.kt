package com.beta.myhbt_api.Network.User

import retrofit2.Call
import retrofit2.http.POST

interface ValidateTokenPostService {
    @POST("/api/v1/users/validateLoginToken")
    fun validate(): Call<Any>
}