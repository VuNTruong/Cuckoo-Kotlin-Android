package com.beta.myhbt_api.Controller.Follows

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewFollowService {
    @POST("/api/v1/cuckooFollow")
    @FormUrlEncoded
    fun createNewFollow(@Field("follower") follower: String, @Field("following") following: String): Call<Any>
}