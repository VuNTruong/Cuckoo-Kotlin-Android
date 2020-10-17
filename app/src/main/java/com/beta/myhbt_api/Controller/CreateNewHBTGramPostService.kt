package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewHBTGramPostService {
    @POST("/api/v1/hbtGramPost")
    @FormUrlEncoded
    fun createNewHBTGramPost(@Field("content") content: String, @Field("writer") writer: String, @Field("numOfImages") numOfImages: Int): Call<Any>
}