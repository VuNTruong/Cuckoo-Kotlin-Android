package com.beta.myhbt_api.Network.UserStats

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface UpdateUserPhotoLabelVisitService {
    @POST("/api/v1/cuckooPostPhoto/createOrUpdateCuckooPhotoLabelVisit")
    fun updatePhotoLabelVisit(@Query("userId") userId: String, @Query("photoLabel") photoLabel: String): Call<Any>
}