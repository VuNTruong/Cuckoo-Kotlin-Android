package com.beta.myhbt_api.Model

class HBTGramPostComment (content: String, writer: String) {
    // Info of the comment
    val content = content
    val writer = writer

    // Getters
    fun getCommentContent () : String {
        return content
    }

    fun getCommentWriter () : String {
        return writer
    }
}