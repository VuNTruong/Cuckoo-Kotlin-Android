package com.beta.myhbt_api.Controller.User

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginPostDataService {
    @POST("/api/v1/users/login")
    @FormUrlEncoded
    fun login(@Field("email") email: String, @Field("password") password: String): Call<Any>
}