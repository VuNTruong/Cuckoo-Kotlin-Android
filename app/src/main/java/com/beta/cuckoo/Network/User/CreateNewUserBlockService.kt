package com.beta.cuckoo.Network.User

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewUserBlockService {
    @POST("/api/v1/userBlock")
    @FormUrlEncoded
    fun createNewUserBlock(@Field("user") user: String, @Field("blockedBy") blockedBy: String, @Field("blockType") blockType: String): Call<Any>
}