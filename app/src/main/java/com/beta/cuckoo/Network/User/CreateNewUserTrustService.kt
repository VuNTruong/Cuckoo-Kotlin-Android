package com.beta.cuckoo.Network.User

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewUserTrustService {
    @POST("/api/v1/userTrust")
    @FormUrlEncoded
    fun createNewUserTrust(@Field("user") user: String, @Field("trustedBy") trustedBy: String): Call<Any>
}