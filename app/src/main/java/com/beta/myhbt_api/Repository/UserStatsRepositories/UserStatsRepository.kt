package com.beta.myhbt_api.Repository.UserStatsRepositories

import android.content.Context
import com.beta.myhbt_api.Network.RetrofitClientInstance
import com.beta.myhbt_api.Model.UserCommentInteraction
import com.beta.myhbt_api.Model.UserInteraction
import com.beta.myhbt_api.Model.UserLikeInteraction
import com.beta.myhbt_api.Model.UserProfileVisit
import com.beta.myhbt_api.Network.LikesAndComments.GetUserLikeInteractionStatusService
import com.beta.myhbt_api.Network.UserStats.*
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterUserStatsDetail
import kotlinx.android.synthetic.main.activity_user_stats_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class UserStatsRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The user repository (to get info of the currently logged in user for some uses)
    private val userRepository = UserRepository(executor, context)

    // The function to get brief user stats
    fun getBriefUserStats (callback: (arrayOfUserInteraction: ArrayList<UserInteraction>, arrayOfUserLikeInteraction: ArrayList<UserLikeInteraction>,
                                      arrayOfUserCommentInteraction: ArrayList<UserCommentInteraction>, arrayOfUserProfileVisit: ArrayList<UserProfileVisit>) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the get brief user stats service
                val getBriefUserStatsService: GetBriefUserStatsService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetBriefUserStatsService::class.java)

                // Create the call object in order to perform the call to get user interaction status
                val getUserInteractionStatusServiceCall: Call<Any> = getBriefUserStatsService.getBriefAccountStats(userObject.getId(), 3)

                // Perform the call to get user interaction status
                getUserInteractionStatusServiceCall.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        print("Boom")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // If the response body is not empty it means that the token is valid
                        if (response.body() != null) {
                            val body = response.body()
                            print(body)
                            // Body of the request
                            val responseBody = response.body() as Map<String, Any>

                            // Get data from the response body (array of user interaction)
                            val arrayOfUserInteraction = responseBody["arrayOfUserInteraction"] as ArrayList<UserInteraction>
                            val arrayOfUserLikeInteraction = responseBody["arrayOfUserLikeInteraction"] as ArrayList<UserLikeInteraction>
                            val arrayOfUserCommentInteraction = responseBody["arrayOfUserCommentInteraction"] as ArrayList<UserCommentInteraction>
                            val arrayOfUserProfileVisit = responseBody["arrayOfUserProfileVisit"] as ArrayList<UserProfileVisit>

                            // Call the function to return the arrays of account stats
                            callback(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit)
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to get list of users who interact with currently logged in user the most
    fun getListOfUserInteraction (callback: (arrayOfUserInteraction: ArrayList<UserInteraction>) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the get user interaction service
                val getUserInteractionStatusService: GetUserInteractionStatusService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetUserInteractionStatusService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = getUserInteractionStatusService.getUserInteractionStatus(userObject.getId(), 0)

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

                            // Get data from the response body (array of user interaction)
                            val arrayOfUserInteraction = responseBody["data"] as ArrayList<UserInteraction>

                            // Return array of user interaction via callback function
                            callback(arrayOfUserInteraction)
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to get list of users who like post of the currently logged in user the most
    fun getListOfUserLikeInteraction (callback: (arrayOfUserLikeInteraction: ArrayList<UserLikeInteraction>) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the get user like interaction service
                val getUserLikeInteractionStatusService: GetUserLikeInteractionStatusService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetUserLikeInteractionStatusService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = getUserLikeInteractionStatusService.getUserLikeInteractionStatus(userObject.getId(), 0)

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

                            // Get data from the response body (array of user like interaction)
                            val arrayOfUserLikeInteraction = responseBody["data"] as ArrayList<UserLikeInteraction>

                            // Return array of user like interaction via callback function
                            callback(arrayOfUserLikeInteraction)
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to get list of users who comment on post by the currently logged in user the most
    fun getListUserCommentInteraction (callback: (arrayOfUserCommentInteraction: ArrayList<UserCommentInteraction>) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the get user comment interaction service
                val getUserCommentInteractionStatusService: GetUserCommentInteractionStatusService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetUserCommentInteractionStatusService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = getUserCommentInteractionStatusService.getUserCommentInteractionStatus(userObject.getId(), 0)

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

                            // Get data from the response body (array of user comment interaction)
                            val arrayOfUserCommentInteraction = responseBody["data"] as ArrayList<UserCommentInteraction>

                            // Return array of user comment interaction via callback function
                            callback(arrayOfUserCommentInteraction)
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to get user profile visit
    fun getListOfUserProfileVisit (callback: (arrayOfUserProfileVisit: ArrayList<UserProfileVisit>) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                executor.execute{
                    // Create the get user profile visit service
                    val getUserProfileVisitStatusService: GetUserProfileVisitStatusService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                        GetUserProfileVisitStatusService::class.java)

                    // Create the call object in order to perform the call
                    val call: Call<Any> = getUserProfileVisitStatusService.getUserProfileVisitStatus(userObject.getId(), 0)

                    // Perform the call
                    call.enqueue(object: Callback<Any> {
                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            print("Boom")
                        }

                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            // If the response body is not empty it means that call is successful
                            if (response.body() != null) {
                                // Body of the request
                                val responseBody = response.body() as Map<String, Any>

                                // Get data from the response body (array of user profile visit)
                                val arrayOfUserProfileVisit = responseBody["data"] as ArrayList<UserProfileVisit>

                                // Return array of user profile visit via callback function
                                callback(arrayOfUserProfileVisit)
                            } else {
                                print("Something is not right")
                            }
                        }
                    })
                }
            }
        }
    }

    // The function to update user profile visit from the currently logged in user to other user with specified user id
    fun updateUserProfileVisitFromCurrentUser (userIdGotVisited: String) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Check to see if user shown at this activity is the currently logged in user or not
                if (userIdGotVisited != userObject.getId()) {
                    // If it is not, update
                    // Create the update user profile visit service
                    val updateUserProfileVisitService: UpdateUserProfileVisitService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                        UpdateUserProfileVisitService::class.java)

                    // Create the call object in order to perform the call
                    val call: Call<Any> = updateUserProfileVisitService.updateProfileVisit(userObject.getId(), userObject.getId())

                    // Perform the call
                    call.enqueue(object: Callback<Any> {
                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            print("Boom")
                        }

                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            // If the response body is not empty it means that the token is valid
                            if (response.body() != null) {
                                print("Done")
                            } else {
                                print("Something is not right")
                            }
                        }
                    })
                }
            }
        }
    }
}