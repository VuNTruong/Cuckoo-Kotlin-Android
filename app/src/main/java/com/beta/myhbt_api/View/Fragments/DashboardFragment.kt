package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.GetAllHBTGramPostService
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetInfoOfLatestPostService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPost
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.fragment_dashboard.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {
    // Array of HBTGram posts
    private var hbtGramPosts = ArrayList<HBTGramPost>()

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterHBTGramPost?= null

    // Last updated location of the user
    private lateinit var lastUpdatedLocation: LatLng

    // Location in list for next load (the variable which will keep track of from where to load next posts for the user)
    private var locationInListForNextLoad: Int = 0

    // User id of the currently logged in user
    private var userIdOfCurrentUser: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the recycler view
        hbtGramView.layoutManager = LinearLayoutManager(this@DashboardFragment.context)
        hbtGramView.itemAnimator = DefaultItemAnimator()

        // Call the function to get posts for the user
        getInfoOfCurrentUserAndLoadPosts()
    }

    //*********************************** GET POSTS SEQUENCE ***********************************
    /*
    In this sequence, we will do these things
    1. Get info of the currently logged in user
    2. Get current location of the user (already done earlier)
    3. Get info of the latest post in the database (to start loading)
     */

    // The function to get info of the current user
    private fun getInfoOfCurrentUserAndLoadPosts () {
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
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user id of the current user
                    val userId = data["_id"] as String

                    // Update user id property of this activity
                    userIdOfCurrentUser = userId
                    //---------------- Get last updated location of the user ----------------
                    // Get last updated location of the current user
                    val locationObject = data["location"] as Map<String, Any>
                    val coordinatesArray = locationObject["coordinates"] as ArrayList<Double>

                    // Get the latitude
                    val latitude = coordinatesArray[1]

                    // Get the longitude
                    val longitude = coordinatesArray[0]

                    // Create the location object for the last updated location of the current user
                    val center = LatLng(latitude, longitude)

                    // Update the user last updated location property of this activity
                    lastUpdatedLocation = center
                    //---------------- End get last updated location of the user ----------------

                    // Call the function to get info of the latest post
                    getInfoOfLatestPost()
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to get info of the latest post in the database
    private fun getInfoOfLatestPost () {
        // Create the get latest post service
        val getLatestPostService: GetInfoOfLatestPostService = RetrofitClientInstance.getRetrofitInstance(this@DashboardFragment.context!!)!!.create(
            GetInfoOfLatestPostService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getLatestPostService.getLatestPostInfo()

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

                    // Get data from the response body (order in collection of the latest post)
                    // We also add 1 to it so that latest post maybe included as well (if it is for the user)
                    val latestPostOrderInCollection = (responseBody["data"] as Double).toInt() + 1

                    // Call the function to get posts for user
                    getAllPost(userIdOfCurrentUser, latestPostOrderInCollection)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to get all posts from the database for user
    private fun getAllPost (userId: String, latestPostOrderInCollection: Int) {
        // Create the get all posts service
        val getAllPostService: GetAllHBTGramPostService = RetrofitClientInstance.getRetrofitInstance(this@DashboardFragment.context!!)!!.create(
            GetAllHBTGramPostService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getAllPostService.getAllPosts(userId, latestPostOrderInCollection,
            "${lastUpdatedLocation.longitude},${lastUpdatedLocation.latitude}", 50)

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

                    // Get data from the response body (array of posts)
                    val hbtGramPostsArray = ((responseBody["data"] as Map<String, Any>)["documents"]) as ArrayList<HBTGramPost>

                    // Get new order in collection to load next series of posts
                    val newCurrentLocationInList = (((responseBody["data"] as Map<String, Any>)["newCurrentLocationInList"]) as Double).toInt()

                    // Update new current location in list (location in list for next load)
                    // If order in collection to load next series of post is null, let it be 0
                    locationInListForNextLoad = newCurrentLocationInList + 1

                    // Update the array list of posts
                    hbtGramPosts = hbtGramPostsArray

                    // Update the adapter
                    adapter = RecyclerViewAdapterHBTGramPost(hbtGramPosts, this@DashboardFragment.requireActivity(), this@DashboardFragment)

                    // Add adapter to the RecyclerView
                    hbtGramView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //*********************************** END GET POSTS SEQUENCE ***********************************

    //*********************************** LOAD MORE POSTS SEQUENCE ***********************************
    // The function to load more posts for the user based on current location in collection of the user
    fun loadMorePost () {
        // Create the get all posts service
        val getAllPostService: GetAllHBTGramPostService = RetrofitClientInstance.getRetrofitInstance(this@DashboardFragment.context!!)!!.create(
            GetAllHBTGramPostService::class.java)

        // Before performing the call, we will need to check and see if new current location is 0 or not
        // if it is 0, don't do anything and get out of the function
        if (locationInListForNextLoad == 0) {
            // Show alert to the user
            Toast.makeText(this.requireActivity(), "No more posts to load", Toast.LENGTH_SHORT).show()

            // Get out of the function
            return
        }

        // Create the call object in order to perform the call
        val call: Call<Any> = getAllPostService.getAllPosts(userIdOfCurrentUser, locationInListForNextLoad,
            "${lastUpdatedLocation.longitude},${lastUpdatedLocation.latitude}", 50)

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

                    // Get data from the response body (array of posts)
                    val hbtGramPostsArray = ((responseBody["data"] as Map<String, Any>)["documents"]) as ArrayList<HBTGramPost>

                    // Get new order in collection to load next series of posts
                    val newCurrentLocationInList = (((responseBody["data"] as Map<String, Any>)["newCurrentLocationInList"]) as Double).toInt()

                    // Update new current location in list (location in list for next load)
                    locationInListForNextLoad = newCurrentLocationInList + 1

                    // Update the array list of posts
                    hbtGramPosts.addAll(hbtGramPostsArray)

                    // Reload the RecyclerView
                    hbtGramView.adapter!!.notifyDataSetChanged()
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //*********************************** END LOAD MORE POSTS SEQUENCE ***********************************

    // The function to reload the RecyclerView
    fun reloadRecyclerView () {
        hbtGramView.adapter!!.notifyDataSetChanged()
    }
}