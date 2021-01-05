package com.beta.myhbt_api.View.UserStats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Model.UserCommentInteraction
import com.beta.myhbt_api.Model.UserInteraction
import com.beta.myhbt_api.Model.UserLikeInteraction
import com.beta.myhbt_api.Model.UserProfileVisit
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterUserStatsDetail
import kotlinx.android.synthetic.main.activity_user_stats_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserStatsDetail : AppCompatActivity() {
    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterUserStatsDetail ?= null

    // The variable which will keep track of which kind of user stats status to be shown at this activity
    private var userStatsKindToShow = ""

    // User id of the currently logged in user
    private var currentUserId = ""

    // Array of user interaction status objects to be shown to the user (if the activity suppose to show it)
    private var arrayOfUserInteraction = ArrayList<UserInteraction>()

    // Array of user like interaction status objects to be shown to the user
    private var arrayOfUserLikeInteraction = ArrayList<UserLikeInteraction>()

    // Array of user comment interaction status objects to be shown to the user
    private var arrayOfUserCommentInteraction = ArrayList<UserCommentInteraction>()

    // Array of user profile visit objects to be shown to the user
    private var arrayOfUserProfileVisit = ArrayList<UserProfileVisit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_stats_detail)

        // Get which kind of user stats detail to load from previous activity
        userStatsKindToShow = intent.getStringExtra("userStatsKindToShow")!!

        // Instantiate the recycler view
        userStatsDetailView.layoutManager = LinearLayoutManager(applicationContext)
        userStatsDetailView.itemAnimator = DefaultItemAnimator()

        // Call the function to get info of the currently logged in user and load user stats detail
        getInfoOfCurrentUserAndLoadUserStats()
    }

    //**************************** GET INFO OF CURRENT USER SEQUENCE ****************************
    // The function to get info of the currently logged in user
    private fun getInfoOfCurrentUserAndLoadUserStats () {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetCurrentlyLoggedInUserInfoService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getCurrentlyLoggedInUserInfoService.getCurrentUserInfo()

        // Perform the call
        call.enqueue(object: Callback<Any> {
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

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user id in the database of the currently logged in user
                    val userId = data["_id"] as String

                    // Load user id into the user id property of this activity
                    currentUserId = userId

                    // Based on which kind user stats info to load to load to right thing
                    when (userStatsKindToShow) {
                        "userInteraction" -> {
                            // Call the function to load user interaction
                            getUserInteraction()
                        }
                        "userLikeInteraction" -> {
                            getUserLikeInteraction()
                        }
                        "userCommentInteraction" -> {
                            getUserCommentInteraction()
                        }
                        "userProfileVisit" -> {
                            getUserProfileVisit()
                        }
                    }
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //**************************** END GET INFO OF CURRENT USER SEQUENCE ****************************

    //**************************** GET USER DETAIL STATS INFO SEQUENCE ****************************
    // The function to get list of user interaction for the currently logged in user
    private fun getUserInteraction () {
        // Create the get user interaction service
        val getUserInteractionStatusService: GetUserInteractionStatusService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetUserInteractionStatusService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserInteractionStatusService.getUserInteractionStatus(currentUserId, 0)

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
                    val data = responseBody["data"] as ArrayList<UserInteraction>

                    // Update the array of user interaction
                    arrayOfUserInteraction = data

                    // Update the adapter
                    adapter = RecyclerViewAdapterUserStatsDetail(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsDetail)

                    // Add adapter to the RecyclerView
                    userStatsDetailView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to get list of user like interaction for the currently logged in user
    private fun getUserLikeInteraction () {
        // Create the get user like interaction service
        val getUserLikeInteractionStatusService: GetUserLikeInteractionStatusService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetUserLikeInteractionStatusService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserLikeInteractionStatusService.getUserLikeInteractionStatus(currentUserId, 0)

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
                    val data = responseBody["data"] as ArrayList<UserLikeInteraction>

                    // Update the array of user like interaction
                    arrayOfUserLikeInteraction = data

                    // Update the adapter
                    adapter = RecyclerViewAdapterUserStatsDetail(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsDetail)

                    // Add adapter to the RecyclerView
                    userStatsDetailView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to get list of user comment interaction for the currently logged in user
    private fun getUserCommentInteraction () {
        // Create the get user comment interaction service
        val getUserCommentInteractionStatusService: GetUserCommentInteractionStatusService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetUserCommentInteractionStatusService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserCommentInteractionStatusService.getUserCommentInteractionStatus(currentUserId, 0)

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
                    val data = responseBody["data"] as ArrayList<UserCommentInteraction>

                    // Update the array of user comment interaction
                    arrayOfUserCommentInteraction = data

                    // Update the adapter
                    adapter = RecyclerViewAdapterUserStatsDetail(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsDetail)

                    // Add adapter to the RecyclerView
                    userStatsDetailView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to get list of user profile visits for the currently logged in user
    private fun getUserProfileVisit () {
        // Create the get user profile visit service
        val getUserProfileVisitStatusService: GetUserProfileVisitStatusService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetUserProfileVisitStatusService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserProfileVisitStatusService.getUserProfileVisitStatus(currentUserId, 0)

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
                    val data = responseBody["data"] as ArrayList<UserProfileVisit>

                    // Update the array of user profile visit
                    arrayOfUserProfileVisit = data

                    // Update the adapter
                    adapter = RecyclerViewAdapterUserStatsDetail(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsDetail)

                    // Add adapter to the RecyclerView
                    userStatsDetailView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //**************************** END GET USER DETAIL STATS INFO SEQUENCE ****************************
}
