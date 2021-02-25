package com.beta.cuckoo.Network.User

import retrofit2.Call
import retrofit2.http.*
import java.io.Serializable

interface UpdateUserLocationService {
    @PATCH("/api/v1/users/updateMe")
    fun updateUserLocation (@Body body: HashMap<String, HashMap<String, Serializable>> , @Query("userId") userId: String): Call<Any>
}