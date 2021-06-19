package com.beta.cuckoo.Repository.UserRepositories

import android.content.Context
import com.beta.cuckoo.Network.Follows.*
import com.beta.cuckoo.Network.RetrofitClientInstance
import com.beta.cuckoo.Repository.NotificationRepositories.NotificationRepository
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

    // The function to check and see if user with specified user id follows the currently logged in user or not
    fun checkFollowStatusBetweenSpecifiedUserAndCurrentUser (userId: String, callback: (followStatus: String) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the get follow status service
                val getFollowStatusService: GetFollowStatusService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetFollowStatusService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = getFollowStatusService.getFollowStatus(userId, userObject.getId())

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

                            // Return follow status via callback function
                            callback(data)
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to get list of followings of the currently logged in user
    fun getLisOfFollowingsOfCurrentUser (callback: (arrayOfUserId: ArrayList<String>) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                executor.execute {
                    // Create the service for getting number of followings
                    val getArrayOfFollowingService: GetFollowingService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                        GetFollowingService::class.java)

                    // Create the call object in order to perform the call
                    val call: Call<Any> = getArrayOfFollowingService.getFollowings(userObject.getId())

                    // Perform the call
                    call.enqueue(object : Callback<Any> {
                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            print("There seem to be an error ${t.stackTrace}")
                        }

                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            // If the response body is not empty it means that there is data
                            if (response.body() != null) {
                                // Array of user id of followings
                                val arrayOfFollowingsUserId = ArrayList<String>()

                                // Body of the request
                                val responseBody = response.body() as Map<String, Any>

                                // Get data of the response
                                val data = responseBody["data"] as Map<String, Any>

                                // Get list of followings
                                val listOfFollowings = data["documents"] as ArrayList<Map<String, Any>>

                                // Loop through that list of followings, get follower info based on their id
                                for (following in listOfFollowings) {
                                    // Add user id of following to the array of following user id
                                    arrayOfFollowingsUserId.add(following["following"] as String)
                                }

                                // Return array of user ids of followings via callback function
                                callback(arrayOfFollowingsUserId)
                            }
                        }
                    })
                }
            }
        }
    }

    // The function to get list of 2 ways follow of currently logged in user
    fun getListOf2WaysFollowOfCurrentUser (callback: (arrayOfUserId: ArrayList<String>) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the service for getting array of 2 ways follow of currently logged in user
                val getListOf2WaysFollowService: GetListOfUsersToBePinnedOnMap = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetListOfUsersToBePinnedOnMap::class.java
                )

                // Create the call object to perform the call
                val call: Call<Any> = getListOf2WaysFollowService.getListOf2WaysFollow(userObject.getId())

                // Perform the call
                call.enqueue(object : Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        print("There seem to be an error")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from response body (list of 2 ways follow)
                        val data = responseBody["data"] as ArrayList<String>

                        // Return array of users of 2 ways follow via callback function
                        callback(data)
                    }
                })
            }
        }
    }

    // The function to get list of followers of the currently logged in user
    fun getListOfFollowersOfCurrentUser (callback: (arrayOfUserId: ArrayList<String>) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the service for getting array of followers (we will get number of followers based on that)
                val getArrayOfFollowersService: GeteFollowerService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GeteFollowerService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = getArrayOfFollowersService.getFollowers(userObject.getId())

                // Perform the call
                call.enqueue(object : Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        print("There seem to be an error ${t.stackTrace}")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // If the response body is not empty it means that there is data
                        if (response.body() != null) {
                            // Array of user id of followers
                            val arrayOfFollowerUserId = ArrayList<String>()

                            // Body of the request
                            val responseBody = response.body() as Map<String, Any>

                            // Get data of the response
                            val data = responseBody["data"] as Map<String, Any>

                            // Get list of followers
                            val listOfFollowers = data["documents"] as ArrayList<Map<String, Any>>

                            // Loop through that list of followers, get follower info based on their id
                            for (follower in listOfFollowers) {
                                // Add user id of follower to the array of follower user id
                                arrayOfFollowerUserId.add(follower["follower"] as String)
                            }

                            // Return list of follower user id via callback function
                            callback(arrayOfFollowerUserId)
                        }
                    }
                })
            }
        }
    }
}