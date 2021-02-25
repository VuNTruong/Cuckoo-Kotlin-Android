package com.beta.myhbt_api.Network.User

import retrofit2.Call
import retrofit2.http.GET

interface GetCurrentlyLoggedInUserInfoService {
    @GET("/api/v1/users/getUserInfoBasedOnToken")
    fun getCurrentUserInfo(): Call<Any>
}