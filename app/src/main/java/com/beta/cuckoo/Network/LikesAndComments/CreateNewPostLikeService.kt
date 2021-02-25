package com.beta.cuckoo.Network.LikesAndComments

import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query

interface CreateNewPostLikeService {
    @POST("/api/v1/cuckooPostLike/checkLikeStatusAndCreateLike")
    fun createNewHBTGramPostLike(@Query("whoLike") whoLike: String, @Query("postId") postId: String): Call<Any>
}