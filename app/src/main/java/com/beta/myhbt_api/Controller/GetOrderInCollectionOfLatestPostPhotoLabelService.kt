package com.beta.myhbt_api.Controller

import retrofit2.Call
import retrofit2.http.GET

interface GetOrderInCollectionOfLatestPostPhotoLabelService {
    @GET("/api/v1/hbtGramPostPhoto/getLatestPhotoLabelOrderInCollection")
    fun getOrderInCollectionOfLatestPostPhoto(): Call<Any>
}