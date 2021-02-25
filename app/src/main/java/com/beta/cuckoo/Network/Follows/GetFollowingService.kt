package com.beta.cuckoo.Network.Follows

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetFollowingService {
    @GET("/api/v1/cuckooFollow")
    fun getFollowings(@Query("follower") userId: String): Call<Any>
}