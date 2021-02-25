package com.beta.myhbt_api.Network.UserStats

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface UpdateUserProfileVisitService {
    @POST("/api/v1/cuckooAccountStats/updateProfileVisit")
    fun updateProfileVisit(@Query("visitorUserId") visitorUserId: String, @Query("visitedUserId") visitedUserId: String): Call<Any>
}