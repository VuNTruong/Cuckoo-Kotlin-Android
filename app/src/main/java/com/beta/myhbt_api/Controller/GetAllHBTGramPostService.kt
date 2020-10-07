package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET

interface GetAllHBTGramPostService {
    @GET("/api/v1/hbtGramPost")
    fun getAllPosts(): Call<Any>
}