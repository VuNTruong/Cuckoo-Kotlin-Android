package com.beta.myhbt_api.Model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class HBTGramPost (_id: String, content: String, writer: String, numOfImages: Int, orderInCollection: Int, dateCreated: String) :
    Serializable {
    // Attributes of the post
    @SerializedName("_id")
    private val _id: String = _id

    @SerializedName("content")
    private val content: String = content

    @SerializedName("writer")
    private val writer: String = writer

    @SerializedName("numOfImages")
    private val numOfImages: Int = numOfImages

    @SerializedName("orderInCollection")
    private val orderInCollection: Int = orderInCollection

    @SerializedName("dateCreated")
    private val dateCreated: String = dateCreated

    // Getters
    fun getId() : String {
        return _id
    }

    fun getContent() : String {
        return content
    }

    fun getWriter() : String {
        return writer
    }

    fun getNumOfImages() : Int {
        return numOfImages
    }

    fun getOrderInCollection() : Int {
        return orderInCollection
    }

    fun getDateCreated() : String {
        return dateCreated
    }
}