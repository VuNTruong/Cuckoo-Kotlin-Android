package com.beta.cuckoo.Network.User

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetUserBlockBetween2UsersService {
    @GET("/api/v1/userBlock")
    fun getUserBlockBetween2Users(@Query("user") user: String, @Query("blockedBy") blockedBy: String): Call<Any>
}