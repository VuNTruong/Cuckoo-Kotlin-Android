package com.beta.cuckoo.Network.UserStats

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface UpdateUserLikeInteractionService {
    @POST("/api/v1/cuckooAccountStats/updateLikeStatus")
    fun updateUserLikeInteraction(@Query("userId") userId: String): Call<Any>
}