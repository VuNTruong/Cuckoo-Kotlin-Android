package com.beta.cuckoo.Repository.VideoChatRepository

import android.content.Context
import com.beta.cuckoo.Network.RetrofitClientInstance
import com.beta.cuckoo.Network.VideoChat.CreateVideoChatRoomService
import com.beta.cuckoo.Network.VideoChat.DeleteVideoChatRoomService
import com.beta.cuckoo.Network.VideoChat.RequestForVideoChatRoomAccessTokenService
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class VideoChatRepository (executor: Executor, context: Context) {
    // The user repository (to get info of the currently logged in user for some uses)
    private val userRepository = UserRepository(executor, context)

    // In order to prevent us from encountering the class cast exception, we need to do the following
    // Create the GSON object
    private val gs = Gson()

    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The function to get access token to get into video chat room for user with specified user id and room name
    fun getAccessTokenIntoVideoChatRoom (chatRoomName: String, userId: String, callback: (accessToken: String) -> Unit) {
        executor.execute {
            // Create the request access token service
            val requestForVideoChatRoomAccessTokenService: RequestForVideoChatRoomAccessTokenService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                RequestForVideoChatRoomAccessTokenService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = requestForVideoChatRoomAccessTokenService.requestForVideoChatRoomAccessToken(userId, chatRoomName)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get the access token
                        val accessToken = responseBody["accessToken"] as String

                        // Return access token via callback function
                        callback(accessToken)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to create a video chat room based on specified room name
    fun createRoom (chatRoomName: String, callback: (isExisted: Boolean, isCreated: Boolean) -> Unit) {
        executor.execute {
            // Create the create room service
            val createVideoChatRoomService: CreateVideoChatRoomService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                CreateVideoChatRoomService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = createVideoChatRoomService.createVideoChatRoom(chatRoomName)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print(t.stackTrace)
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response code is 201, room is created
                    if (response.code() == 201) {
                        // Call the function to let client know that room is not existed and is created
                        callback(false, true)
                    } else {
                        // Call the function to let client know that room is existed and will not be created
                        callback(true, false)
                    }
                }
            })
        }
    }

    // The function to delete a video chat room
    fun deleteVideoRoomName (chatRoomName: String, callback: (isDeleted: Boolean) -> Unit) {
        executor.execute {
            // Create the delete room service
            val deleteVideoChatRoomService: DeleteVideoChatRoomService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                DeleteVideoChatRoomService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = deleteVideoChatRoomService.deleteVideoChatRoom(chatRoomName)

            // Perform the call
            call.enqueue(object : Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print(t.stackTrace)
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    if (response.code() == 200) {
                        // Call the function to let client know that room has been deleted
                        callback(true)
                    }
                }
            })
        }
    }
}