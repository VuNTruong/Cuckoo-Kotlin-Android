package com.beta.cuckoo.Model

import com.google.gson.annotations.SerializedName

class PostComment (content: String, writer: String, commentId: String) {
    // Info of the comment
    @SerializedName("content")
    val content = content

    @SerializedName("writer")
    val writer = writer

    @SerializedName("_id")
    val commentId = commentId

    // Getters
    fun getCommentContent () : String {
        return content
    }

    fun getCommentWriter () : String {
        return writer
    }

    fun getIdComment () : String {
        return commentId
    }
}