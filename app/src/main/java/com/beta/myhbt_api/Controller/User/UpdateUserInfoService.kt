package com.beta.myhbt_api.Controller.User

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PATCH
import retrofit2.http.Query

interface UpdateUserInfoService {
    @PATCH("/api/v1/users/updateMe")
    @FormUrlEncoded
    fun updateUserInfo(@Field("avatarURL") avatarURL: String, @Field("coverURL") coverURL: String, @Field("phoneNumber") phoneNumber: String,
              @Field("facebook") facebook: String, @Field("instagram") instagram: String, @Field("twitter") twitter: String, @Field("zalo") zalo: String, @Query("userId") userId: String): Call<Any>
}