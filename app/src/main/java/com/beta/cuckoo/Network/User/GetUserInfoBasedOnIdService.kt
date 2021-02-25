package com.beta.cuckoo.Network.User

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetUserInfoBasedOnIdService {
    @GET("/api/v1/users")
    fun getUserInfoBasedOnId(@Query("_id") userId: String): Call<Any>
}