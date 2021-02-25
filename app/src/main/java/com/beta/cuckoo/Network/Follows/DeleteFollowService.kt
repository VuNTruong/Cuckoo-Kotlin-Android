package com.beta.cuckoo.Network.Follows

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Query

interface DeleteFollowService {
    @DELETE("/api/v1/cuckooFollow/deleteHBTGramFollowBetween2Users")
    fun deleteFollow(@Query("follower") follower: String, @Query("following") following: String): Call<Any>
}