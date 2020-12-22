package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Model.UserCommentInteraction
import com.beta.myhbt_api.Model.UserInteraction
import com.beta.myhbt_api.Model.UserLikeInteraction
import com.beta.myhbt_api.Model.UserProfileVisit
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterUserStats
import kotlinx.android.synthetic.main.fragment_user_stats.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserStatsFragment : Fragment() {
    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterUserStats

    // User id of the currently logged in user
    private lateinit var currentUserId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the recycler view
        userStatsView.layoutManager = LinearLayoutManager(this.requireActivity())
        userStatsView.itemAnimator = DefaultItemAnimator()

        // Call the function to get info of the currently logged in user and load user account stats
        getInfoOfCurrentUserAndLoadUserStats()
    }

    //************************ LOAD ACCOUNT STATS SEQUENCE ************************
    // The function to get id of current user which will then check if user at this activity is current or not
    private fun getInfoOfCurrentUserAndLoadUserStats () {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
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

                    // Call the functions to load user stats for the user
                    getBriefAccountStats(currentUserId)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to get brief user interaction
    private fun getBriefAccountStats (userId: String) {
        // Create the get brief user stats service
        val getBriefUserStatsService: GetBriefUserStatsService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
            GetBriefUserStatsService::class.java)

        // Create the call object in order to perform the call to get user interaction status
        val getUserInteractionStatusServiceCall: Call<Any> = getBriefUserStatsService.getBriefAccountStats(userId, 3)

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

                    // Update array of user interaction
                    //arrayOfUserInteraction = data

                    // Update the adapter
                    adapter = RecyclerViewAdapterUserStats(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsFragment.requireActivity())

                    // Add adapter to the RecyclerView
                    userStatsView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }
}