package com.beta.cuckoo.Repository.UserRepositories

import android.content.Context
import com.beta.cuckoo.Model.MessageRoom
import com.beta.cuckoo.Network.RetrofitClientInstance
import com.beta.cuckoo.Network.User.CreateNewUserBlockService
import com.beta.cuckoo.Network.User.DeleteAUserBlockService
import com.beta.cuckoo.Network.User.GetUserBlockBetween2UsersService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class UserBlockRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // User respository
    private val userRepository = UserRepository(executor, context)

    // Create the GSON object
    val gs = Gson()

    // The function to get a block between the 2 users
    fun getBlockBetween2Users (userGetBlocked: String, blockingUser: String, callback: (isBlocked: Boolean) -> Unit) {
        executor.execute {
            // Create the get user block between users service
            val getUserBlockBetween2UsersService: GetUserBlockBetween2UsersService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetUserBlockBetween2UsersService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getUserBlockBetween2UsersService.getUserBlockBetween2Users(userGetBlocked, blockingUser)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty, there is data
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get number of results (number of found block)
                        val results = (responseBody["results"] as Double).toInt()

                        // If results is 0, there is no block between the 2 users
                        if (results == 0) {
                            // Let the view know that there is no block via callback function
                            callback(false)
                        } else {
                            // Let the view know that there is a block via callback function
                            callback(true)
                        }
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to create a new user block
    private fun createNewUserBlock (user: String, blockedBy: String, blockType: String, callback: (isCreated: Boolean) -> Unit) {
        executor.execute {
            // Create the create new user block service
            val createNewUserBlockService: CreateNewUserBlockService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                CreateNewUserBlockService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = createNewUserBlockService.createNewUserBlock(user, blockedBy, blockType)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If response code is 201, it means that user block has been created with no error
                    if (response.code() == 201) {
                        // Let the view know that user block has been created via callback function
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            })
        }
    }

    // The function to delete a user block between the 2 users
    private fun deleteABlockBetween2users (userGetBlocked: String, blockingUser: String, callback: (isDeleted: Boolean) -> Unit) {
        executor.execute {
            // Create the delete a block between 2 users service
            val deleteAUserBlockService: DeleteAUserBlockService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                DeleteAUserBlockService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = deleteAUserBlockService.deleteAUserBlockBetween2Users(userGetBlocked, blockingUser)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If response code is 200, it means that user block has been deleted with no error
                    if (response.code() == 200) {
                        // Let the view know that user block has been created via callback function
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            })
        }
    }

    // The function to check for block status between current user and user with specified user id
    fun checkBlockStatusBetweenCurrentUserAndOtherUser (otherUserId: String, callback: (isBlocked: Boolean) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                getBlockBetween2Users(otherUserId, userObject.getId()) {isBlocked ->
                    // Let the view know that there is a block between the 2 users
                    if (isBlocked) {
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            }
        }
    }

    // The function to create a block between current user and other user with specified user id
    fun createABlockBetweenCurrentUserAndOtherUser (otherUserId: String, blockType: String, callback: (isBlocked: Boolean) -> Unit) {
        executor.execute {
            // Call the function to get user info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                createNewUserBlock(otherUserId, userObject.getId(), blockType) {isCreated ->
                    // Let the view know that block is created via callback function
                    if (isCreated) {
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            }
        }
    }

    // The function to delete a block between current user and other user with specified user id
    fun deleteABlockBetweenCurrentUserAndOtherUser (otherUserId: String, callback: (isDeleted: Boolean) -> Unit) {
        executor.execute {
            // Call the function to get user info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                deleteABlockBetween2users(otherUserId, userObject.getId()) {isDeleted ->
                    // Let the view know that block has been deleted via callback function
                    if (isDeleted) {
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            }
        }
    }
}