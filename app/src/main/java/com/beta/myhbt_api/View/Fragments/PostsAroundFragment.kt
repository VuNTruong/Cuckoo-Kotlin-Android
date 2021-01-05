package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Interfaces.PostShowingInterface
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPost
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.fragment_dashboard.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostsAroundFragment : Fragment(), PostShowingInterface {
    // Array of HBTGram posts nearby
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

        // Show the loading layout and hide the recycler view at beginning
        loadingLayoutHomePage.visibility = View.VISIBLE
        hbtGramView.visibility = View.INVISIBLE

        // Instantiate the recycler view
        hbtGramView.layoutManager = LinearLayoutManager(this@PostsAroundFragment.context)
        hbtGramView.itemAnimator = DefaultItemAnimator()

        // Update the adapter
        adapter = RecyclerViewAdapterHBTGramPost(hbtGramPosts, this@PostsAroundFragment.requireActivity(), this@PostsAroundFragment)

        // Add adapter to the RecyclerView
        hbtGramView.adapter = adapter

        // Call the function to get posts for the user
        getInfoOfCurrentUserAndLoadPosts()
    }

    //*************************** GET INFO OF CURRENTLY LOGGED IN USER SEQUENCE ***************************
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

                    // Call the function to get info of the latest post and load posts around
                    getInfoOfLatestPost()
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //*************************** END GET INFO OF CURRENTLY LOGGED IN USER SEQUENCE ***************************

    //*************************** GET POSTS AROUND SEQUENCE ***************************
    /*
    In this sequence, we will do 2 things
    1. Get order in collection of latest post in collection
    2. Start loading posts from that location
     */

    // The function to get info of the latest post in the database
    private fun getInfoOfLatestPost () {
        // Create the get latest post service
        val getLatestPostService: GetInfoOfLatestPostService = RetrofitClientInstance.getRetrofitInstance(this@PostsAroundFragment.context!!)!!.create(
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
                    val latestPostOrderInCollection = (responseBody["data"] as Double).toInt()

                    // Update location to start loading
                    locationInListForNextLoad = latestPostOrderInCollection

                    // Call the function to start loading posts
                    getPostsAround()
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to start loading posts (when loading more posts, just need to call this one)
    private fun getPostsAround () {
        // Create the get posts around service
        val getPostsWithinARadiusService: GetPostsWithinARadiusService = RetrofitClientInstance.getRetrofitInstance(this@PostsAroundFragment.context!!)!!.create(
            GetPostsWithinARadiusService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getPostsWithinARadiusService.getPostsWithinARadius("${lastUpdatedLocation.latitude},${lastUpdatedLocation.longitude}", 50, locationInListForNextLoad)

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
                    val hbtGramPostsArray = responseBody["data"] as ArrayList<HBTGramPost>

                    // Get new order in collection to load next series of posts
                    val newCurrentLocationInList = (responseBody["newCurrentLocationInList"] as Double).toInt()

                    // Update new current location in list (location in list for next load)
                    // If order in collection to load next series of post is null, let it be 0
                    locationInListForNextLoad = newCurrentLocationInList

                    // Update the array list of posts
                    hbtGramPosts.addAll(hbtGramPostsArray)

                    // Update the RecyclerView
                    hbtGramView.adapter!!.notifyDataSetChanged()

                    // Show the recycler view and hide the loading layout when done at beginning
                    loadingLayoutHomePage.visibility = View.INVISIBLE
                    hbtGramView.visibility = View.VISIBLE
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //*************************** END GET POSTS AROUND SEQUENCE ***************************

    //*********************************** IMPLEMENT ABSTRACT FUNCTION OF THE INTERFACE TO LOAD MORE POSTS ***********************************
    override fun loadMorePosts() {
        // Call the function to load more posts
        getPostsAround()
    }
    //*********************************** END IMPLEMENT ABSTRACT FUNCTION OF THE INTERFACE TO LOAD MORE POSTS ***********************************

    //******************************** CREATE NOTIFICATION SEQUENCE (IMPLEMENTED FROM INTERFACE) ********************************
    // The function to create new notification. It should load first photo of post first
    override fun createNotification (content: String, forUser: String, fromUser: String, image: String, postId: String) {
        // Create the get first image URL service
        val getFirstImageURLService: GetFirstImageURLOfPostService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
            GetFirstImageURLOfPostService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getFirstImageURLService.getFirstPhotoURL(postId)

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

                    // Get the array of images
                    val arrayOfImages = data["documents"] as List<Map<String, Any>>

                    if (arrayOfImages.isNotEmpty()) {
                        // Get image info from the received data
                        val firstImageInfo = (data["documents"] as List<Map<String, Any>>)[0]

                        // Get URL of the image
                        val firstImageURL = firstImageInfo["imageURL"] as String

                        // Call the function to actually create the notification
                        sendNotification(content, forUser, fromUser, firstImageURL, postId)
                    }
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to send notification
    private fun sendNotification (content: String, forUser: String, fromUser: String, image: String, postId: String) {
        // Create the create notification service
        val createNotificationService: CreateNotificationService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
            CreateNotificationService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = createNotificationService.createNewNotification(content, forUser, fromUser, image, postId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {

            }
        })
    }
    //******************************** END CREATE NOTIFICATION SEQUENCE (IMPLEMENTED FROM INTERFACE) ********************************
}