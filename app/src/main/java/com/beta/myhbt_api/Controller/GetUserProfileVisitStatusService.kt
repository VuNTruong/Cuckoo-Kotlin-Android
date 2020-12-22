package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetUserProfileVisitStatusService {
    @GET("/api/v1/hbtGramAccountStats/getProfileVisitStatus")
    fun getUserProfileVisitStatus(@Query("userId") userId: String, @Query("limit") limit: Int): Call<Any>
}