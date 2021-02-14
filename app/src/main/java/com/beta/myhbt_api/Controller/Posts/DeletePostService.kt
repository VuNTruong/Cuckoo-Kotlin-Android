package com.beta.myhbt_api.Controller.Posts

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Query

interface DeletePostService {
    @DELETE("/api/v1/cuckooPost")
    fun deletePost(@Query("postId") postId: String): Call<Any>
}