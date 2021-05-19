package com.beta.cuckoo.Repository.UserRepositories

import android.content.Context
import com.beta.cuckoo.Network.RetrofitClientInstance
import com.beta.cuckoo.Network.User.CreateNewUserTrustService
import com.beta.cuckoo.Network.User.DeleteAUserTrustService
import com.beta.cuckoo.Network.User.GetUserTrustBetween2UsersService
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class UserTrustRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // User respository
    private val userRepository = UserRepository(executor, context)

    // Create the GSON object
    val gs = Gson()

    // The function to get a trust between the 2 users
    private fun getTrustBetween2Users (userGetTrusted: String, trustingUser: String, callback: (isTrusted: Boolean) -> Unit) {
        executor.execute {
            // Create the get user trust between users service
            val getUserTrustBetween2UsersService: GetUserTrustBetween2UsersService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetUserTrustBetween2UsersService::class.java
            )

            // Create the call object in order to perform the call
            val call: Call<Any> = getUserTrustBetween2UsersService.getUserTrustBetween2Users(userGetTrusted, trustingUser)

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

                        // If results is 0, there is no trust between the 2 users
                        if (results == 0) {
                            // Let the view know that there is no block via callback function
                            callback(false)
                        } else {
                            // Let the view know that there is a trust via callback function
                            callback(true)
                        }
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to delete a user trust between the 2 users
    private fun deleteATrustBetween2Users (userGetTrusted: String, trustingUser: String, callback: (isDeleted: Boolean) -> Unit) {
        executor.execute {
            // Create the delete a trust between 2 users service
            val deleteAUserTrustService: DeleteAUserTrustService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                DeleteAUserTrustService::class.java
            )

            // Create the call object in order to perform the call
            val call: Call<Any> = deleteAUserTrustService.deleteAUserTrustBetween2Users(userGetTrusted, trustingUser)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If response code is 200, it means that user trust has been deleted with no error
                    if (response.code() == 200) {
                        // Let the view know that user trust has been created via callback function
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            })
        }
    }

    // The function to create a new user trust
    private fun createNewUserTrust (user: String, trustedBy: String, callback: (isCreated: Boolean) -> Unit) {
        executor.execute {
            // Create the create mew user trust service
            val createNewUserTrustService: CreateNewUserTrustService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                CreateNewUserTrustService::class.java
            )

            // Create the call object in order to perform the call
            val call: Call<Any> = createNewUserTrustService.createNewUserTrust(user, trustedBy)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If response code is 201, it means that user trust has been created with no error
                    if (response.code() == 201) {
                        // Let the view know that user trust has been created via callback function
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            })
        }
    }

    // The function to check for trust status between current user and user with specified user id
    fun checkTrustStatusBetweenCurrentUserAndOtherUser (otherUserId: String, callback: (isTrusted: Boolean) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                getTrustBetween2Users(otherUserId, userObject.getId()) {isTrusted ->
                    // Let the view know if there is a trust between the 2 users or not
                    callback(isTrusted)
                }
            }
        }
    }

    // The function to check for trust status between user with specified user id and current user
    fun checkTrustStatusBetweenOtherUserAndCurrentUser (otherUserId: String, callback: (isTrusted: Boolean) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser {userObject ->
                getTrustBetween2Users(userObject.getId(), otherUserId) {isTrusted ->
                    // Let the view know if there is a trust between the 2 users or not
                    callback(isTrusted)
                }
            }
        }
    }

    // The function to create a trust between current user other user with specified user id
    fun createATrustBetweenCurrentUserAndOtherUser (otherUserId: String, callback: (isCreated: Boolean) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                createNewUserTrust(otherUserId, userObject.getId()) {isCreated ->
                    // Let the view know if trust is created or not via callback function
                    callback(isCreated)
                }
            }
        }
    }

    // The function to delete a trust between current user and other user with specified user id
    fun deleteATrustBetweenCurrentUserAndOtherUser (otherUserId: String, callback: (isDeleted: Boolean) -> Unit) {
        executor.execute {
            // Call the function to get user info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                deleteATrustBetween2Users(otherUserId, userObject.getId()) {isDeleted ->
                    // Let the view if trust is deleted or not via callback function
                    callback(isDeleted)
                }
            }
        }
    }
}