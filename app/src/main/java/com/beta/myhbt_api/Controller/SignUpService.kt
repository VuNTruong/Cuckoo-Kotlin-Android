package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Query

interface SignUpService {
    @POST("/api/v1/users/signup")
    @FormUrlEncoded
    fun signUp(@Field("email") email: String, @Field("password") password: String, @Field("passwordConfirm") passwordConfirm: String, @Field("firstName") firstName: String,
               @Field("middleName") middleName: String, @Field("lastName") lastName: String, @Field("role") role: String, @Field("avatarURL") avatarURL: String, @Field("coverURL") coverURL: String): Call<Any>
}