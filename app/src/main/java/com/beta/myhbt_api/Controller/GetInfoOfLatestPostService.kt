package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET

interface GetInfoOfLatestPostService {
    @GET("/api/v1/hbtGramPost/getLatestPostInCollection")
    fun getLatestPostInfo(): Call<Any>
}