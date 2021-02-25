package com.beta.cuckoo.Network.Posts

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetPhotosOfUserService {
    @GET("/api/v1/cuckooPostPhoto/getPhotosOfUser")
    fun getPhotosOfUser(@Query("userId") userId: String): Call<Any>
}