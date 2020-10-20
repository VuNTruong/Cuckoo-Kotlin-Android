package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GetSignUpTokenService {
    @POST("/api/v1/users/getSignUpToken")
    @FormUrlEncoded
    fun getSignUpToken(@Field("userId") userId: String): Call<Any>
}