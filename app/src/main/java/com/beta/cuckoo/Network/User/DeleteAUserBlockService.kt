package com.beta.cuckoo.Network.User

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Query

interface DeleteAUserBlockService {
    @DELETE("/api/v1/userBlock")
    fun deleteAUserBlockBetween2Users(@Query("userGetBlocked") userGetBlocked: String, @Query("blockingUser") blockingUser: String): Call<Any>
}