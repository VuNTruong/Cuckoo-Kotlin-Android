package com.beta.myhbt_api.Network.User

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetUserWithinARadiusService {
    @GET("/api/v1/users/getUserWithin")
    fun getUserWithinARadius(@Query("latlong") location: String, @Query("distance") distance: Int, @Query("unit") unit: String, @Query("fullName") fullName: String): Call<Any>
}