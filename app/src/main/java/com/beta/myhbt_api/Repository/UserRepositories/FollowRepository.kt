package com.beta.myhbt_api.Repository.UserRepositories

import android.content.Context
import com.beta.myhbt_api.Network.Follows.CreateNewFollowService
import com.beta.myhbt_api.Network.Follows.DeleteFollowService
import com.beta.myhbt_api.Network.Follows.GetFollowStatusService
import com.beta.myhbt_api.Network.RetrofitClientInstance
import com.beta.myhbt_api.Repository.NotificationRepositories.NotificationRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class FollowRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The user repository (to get info of the currently logged in user for some uses)
    private val userRepository = UserRepository(executor, context)

    // Notification repository
    private val notificationRepository = NotificationRepository(executor, context)

    // The function to create a follow between the currently logged in user and user with the specified user id
    fun createNewFollow (userId: String, callback: (buttonContent: String) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the create follow object service
                val createNewFollowObjectService: CreateNewFollowService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    CreateNewFollowService::class.java)

                // Create the call object to perform the call
                val call: Call<Any> = createNewFollowObjectService.createNewFollow(userObject.getId(), userId)

                // Perform the call
                call.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        print("Boom")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // If the response body is not empty it means that new follow was created
                        if (response.body() != null) {
                            // Content of the follow button will be "Unfollow". Return it via callback function
                            callback("Unfollow")

                            // Call the function to create new notification
                            notificationRepository.sendNotification("followed", userId, userObject.getId(), "none", "none")
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to remove a follow object between currently logged in user and user with specified user id (Unfollow)
    fun removeAFollow (userId: String, callback: (buttonContent: String) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the delete follow service
                val deleteFollowService: DeleteFollowService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    DeleteFollowService::class.java)

                // Create the call object to perform the call
                val call: Call<Any> = deleteFollowService.deleteFollow(userObject.getId(), userId)

                // Perform the call
                call.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        print("Boom")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // Check the response code
                        // If it is 204. It means that a follow has been removed
                        if (response.code() == 204) {
                            // Content of the follow button will be "Follow". Return it via callback function
                            callback("Follow")
                        }
                    }
                })
            }
        }
    }

    // The function to check follow status between the currently logged in user and user with specified user id
    fun checkFollowStatus (userId: String, callback: (buttonContent: String) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the get follow status service
                val getFollowStatusService: GetFollowStatusService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetFollowStatusService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = getFollowStatusService.getFollowStatus(userObject.getId(), userId)

                // Perform the call
                call.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        print("There seem to be an error ${t.stackTrace}")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // If the response body is not empty it means that the token is valid
                        if (response.body() != null) {
                            // Body of the request
                            val responseBody = response.body() as Map<String, Any>

                            // Get data from the response body (follow status)
                            val data = responseBody["data"] as String

                            // Check the follow status. If it is "Yes", content of the follow button will be "Unfollow"
                            // return this via callback function
                            if (data == "Yes") {
                                callback("Unfollow")
                            } // Otherwise, content of the follow button will be "Follow"
                            else {
                                callback("Follow")
                            }
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }
}