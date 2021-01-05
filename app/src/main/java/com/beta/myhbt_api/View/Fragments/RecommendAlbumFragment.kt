package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetOrderInCollectionOfLatestPostPhotoLabelService
import com.beta.myhbt_api.Controller.GetRecommendedPhotosForUserService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterRecommendAlbum
import kotlinx.android.synthetic.main.fragment_recommend_photo.recommendAlbumView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecommendAlbumFragment: Fragment() {
    // User id of the current user
    private var currentUserId = ""

    // Current location in list (will be mutated each time loaded)
    private var currentLocationInList = 0

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterRecommendAlbum ?= null

    // Array of photos recommended for the user
    private var arrayOfRecommendedPhotos = ArrayList<HBTGramPostPhoto>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recommend_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the recycler view
        recommendAlbumView.layoutManager = LinearLayoutManager(this@RecommendAlbumFragment.context)
        recommendAlbumView.itemAnimator = DefaultItemAnimator()

        // Call the function to get user id of the currently logged in user
        getInfoOfCurrentUser()
    }

    //*********************************** GET INFO OF CURRENT USER SEQUENCE ***********************************
    // The function to get info of the currently logged in user
    private fun getInfoOfCurrentUser () {
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

                    // Get user id in the database of the currently logged in user
                    val userId = data["_id"] as String

                    // Update the current user id property of this activity
                    currentUserId = userId

                    // Call the function to get order in collection of latest photo label and start loading recommended photos
                    // for the user
                    getOrderInCollectionOfLatestPhotoLabel()
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //*********************************** END GET INFO OF CURRENT USER SEQUENCE ***********************************

    //*********************************** GET RECOMMENDED PHOTOS SEQUENCE ***********************************
    /*
    In this sequence, we will do 2 things
    1. Get order in collection of the latest post photo label in collection
    2. Get photos from that point

    If we do need to load more photos, we don't need to reload latest order in collection again, just load latest
    order in collection again if user reload the page
     */

    // The function to get order in collection of latest post photo label in collection
    fun getOrderInCollectionOfLatestPhotoLabel () {
        // Create the get order in collection of latest photo label service
        val getOrderInCollectionOfLatestPostPhotoLabelService: GetOrderInCollectionOfLatestPostPhotoLabelService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
            GetOrderInCollectionOfLatestPostPhotoLabelService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getOrderInCollectionOfLatestPostPhotoLabelService.getOrderInCollectionOfLatestPostPhoto()

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

                    // Get data from the response body (order in collection of latest photo label in collection)
                    val data = (responseBody["data"] as Double).toInt()

                    // Update the current location in list for the user
                    // Add 1 here because we may want to include latest post as well
                    currentLocationInList = data + 1

                    // Call the function to load list of recommended photos for the user
                    getRecommendedPhotos()
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to get list of recommended photos for the user
    fun getRecommendedPhotos () {
        // Create the get recommended list of photos service
        val getRecommendedPhotosForUserService: GetRecommendedPhotosForUserService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
            GetRecommendedPhotosForUserService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getRecommendedPhotosForUserService.getPostPhotosForUser(currentUserId, currentLocationInList)

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

                    // Get data from the response body (list of photos recommended for the user)
                    val data = responseBody["data"] as ArrayList<HBTGramPostPhoto>

                    // Get new current location in list for the user so that the app know where to go next
                    val newCurrentLocationInList = (responseBody["newCurrentLocationInList"] as Double).toInt()

                    // Update the list of recommended photos
                    arrayOfRecommendedPhotos.addAll(data)

                    // Update current location in list
                    currentLocationInList = newCurrentLocationInList

                    // Update the adapter
                    adapter = RecyclerViewAdapterRecommendAlbum(arrayOfRecommendedPhotos, this@RecommendAlbumFragment.requireActivity(), this@RecommendAlbumFragment)

                    // Add adapter to the RecyclerView
                    recommendAlbumView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //*********************************** END GET RECOMMENDED PHOTOS SEQUENCE ***********************************
}