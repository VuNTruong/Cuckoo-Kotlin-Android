package com.beta.myhbt_api.Model

import com.google.gson.annotations.SerializedName

class HBTGramPostComment (content: String, writer: String) {
    // Info of the comment
    @SerializedName("content")
    val content = content

    @SerializedName("writer")
    val writer = writer

    // Getters
    fun getCommentContent () : String {
        return content
    }

    fun getCommentWriter () : String {
        return writer
    }
}