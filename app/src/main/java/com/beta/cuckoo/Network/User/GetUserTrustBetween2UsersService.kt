package com.beta.cuckoo.Network.User

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetUserTrustBetween2UsersService {
    @GET("/api/v1/userTrust")
    fun getUserTrustBetween2Users(@Query("user") user: String, @Query("trustedBy") trustedBy: String): Call<Any>
}