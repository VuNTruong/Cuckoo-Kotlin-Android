package com.beta.cuckoo.Network.LikesAndComments

import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Query

interface DeleteCommentService {
    @DELETE("/api/v1/cuckooPostComment/deleteCommentWithId")
    fun deleteComment(@Query("commentId") commentId: String): Call<Any>
}