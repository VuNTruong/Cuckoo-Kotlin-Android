package com.beta.cuckoo.Network.Follows

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeteFollowerService {
    @GET("/api/v1/cuckooFollow")
    fun getFollowers(@Query("following") userId: String): Call<Any>
}