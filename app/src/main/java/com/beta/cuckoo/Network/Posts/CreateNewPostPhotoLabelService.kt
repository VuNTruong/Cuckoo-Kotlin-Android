package com.beta.cuckoo.Network.Posts

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface CreateNewPostPhotoLabelService {
    @POST("/api/v1/cuckooPostPhotoLabel")
    @FormUrlEncoded
    fun createNewHBTGramPostPhotoLabel(@Field("imageID") imageID: String, @Field("imageLabel") imageLabel: String): Call<Any>
}