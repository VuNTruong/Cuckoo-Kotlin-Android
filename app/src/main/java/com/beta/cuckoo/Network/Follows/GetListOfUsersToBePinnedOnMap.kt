package com.beta.cuckoo.Network.Follows

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetListOfUsersToBePinnedOnMap {
    @GET("/api/v1/users/getUsersToBePinnedOnMap")
    fun getListOf2WaysFollow(@Query("userId") userId: String): Call<Any>
}