package com.beta.cuckoo.Network.Posts

import retrofit2.Call
import retrofit2.http.GET

interface GetInfoOfLatestPostService {
    @GET("/api/v1/cuckooPost/getLatestPostInCollection")
    fun getLatestPostInfo(): Call<Any>
}