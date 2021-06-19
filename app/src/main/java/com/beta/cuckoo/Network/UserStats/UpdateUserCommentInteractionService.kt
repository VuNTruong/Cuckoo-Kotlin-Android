package com.beta.cuckoo.Network.UserStats

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface UpdateUserCommentInteractionService {
    @POST("/api/v1/cuckooAccountStats/updateCommentStatus")
    fun updateUserCommentInteraction(@Query("userId") userId: String): Call<Any>
}