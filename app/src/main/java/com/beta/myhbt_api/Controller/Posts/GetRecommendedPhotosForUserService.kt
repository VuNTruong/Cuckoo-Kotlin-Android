package com.beta.myhbt_api.Controller.Posts

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetRecommendedPhotosForUserService {
    @GET("/api/v1/cuckooPostPhoto/getRecommendedPhotosForUser")
    fun getPostPhotosForUser(@Query("userId") userId: String, @Query("currentLocationInList") currentLocationInList: Int): Call<Any>
}