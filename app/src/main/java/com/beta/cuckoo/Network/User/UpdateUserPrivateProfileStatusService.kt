package com.beta.cuckoo.Network.User

import retrofit2.Call
import retrofit2.http.*

interface UpdateUserPrivateProfileStatusService {
    @PATCH("/api/v1/users/updateMe")
    @FormUrlEncoded
    fun updateUserPrivateProfileStatus(@Field("privateProfile") privateProfile: String, @Query("userId") userId: String): Call<Any>
}