package com.beta.cuckoo.Network.UserStats

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface UpdateUserPhotoLabelVisitService {
    @POST("/api/v1/cuckooPostPhotoLabel/updatePhotoLabelVisit")
    fun updatePhotoLabelVisit(@Query("visitorUserId") userId: String, @Query("visitedPhotoLabel") photoLabel: String): Call<Any>
}