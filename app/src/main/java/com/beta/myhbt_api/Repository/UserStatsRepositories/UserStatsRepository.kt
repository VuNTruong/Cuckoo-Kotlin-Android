package com.beta.myhbt_api.Repository.UserStatsRepositories

import android.content.Context
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Controller.UserStats.GetBriefUserStatsService
import com.beta.myhbt_api.Model.UserCommentInteraction
import com.beta.myhbt_api.Model.UserInteraction
import com.beta.myhbt_api.Model.UserLikeInteraction
import com.beta.myhbt_api.Model.UserProfileVisit
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
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
}