package com.beta.cuckoo.Network.Posts

import retrofit2.Call
import retrofit2.http.GET

interface GetOrderInCollectionOfLatestPostPhotoLabelService {
    @GET("/api/v1/cuckooPostPhoto/getLatestPhotoLabelOrderInCollection")
    fun getOrderInCollectionOfLatestPostPhoto(): Call<Any>
}