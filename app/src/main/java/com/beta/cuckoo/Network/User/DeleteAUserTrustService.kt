package com.beta.cuckoo.Network.User

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Query

interface DeleteAUserTrustService {
    @DELETE("/api/v1/userTrust")
    fun deleteAUserTrustBetween2Users(@Query("userGetTrusted") userGetTrusted: String, @Query("trustingUser") trustingUser: String): Call<Any>
}