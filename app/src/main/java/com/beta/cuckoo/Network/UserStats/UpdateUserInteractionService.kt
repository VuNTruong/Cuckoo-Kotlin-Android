package com.beta.cuckoo.Network.UserStats

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface UpdateUserInteractionService {
    @POST("/api/v1/cuckooAccountStats/updateUserInteractionFrequency")
    fun updateUserInteraction(@Query("userId") userId: String): Call<Any>
}