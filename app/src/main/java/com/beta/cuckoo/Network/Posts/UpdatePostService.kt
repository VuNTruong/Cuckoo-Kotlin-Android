package com.beta.cuckoo.Network.Posts

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PATCH
import retrofit2.http.Query

interface UpdatePostService {
    @PATCH("/api/v1/cuckooPost")
    @FormUrlEncoded
    fun updatePost(@Field("content") content: String, @Query("postId") postId: String): Call<Any>
}