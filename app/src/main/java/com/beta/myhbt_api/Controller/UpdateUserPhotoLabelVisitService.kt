package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface UpdateUserPhotoLabelVisitService {
    @POST("/api/v1/hbtGramPostPhoto/createOrUpdateHBTGramPhotoLabelVisit")
    fun updatePhotoLabelVisit(@Query("userId") userId: String, @Query("photoLabel") photoLabel: String): Call<Any>
}