package com.beta.myhbt_api.Network.LikesAndComments

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetUserLikeInteractionStatusService {
    @GET("/api/v1/cuckooAccountStats/getUserLikeInteractionStatus")
    fun getUserLikeInteractionStatus(@Query("userId") userId: String, @Query("limit") limit: Int): Call<Any>
}