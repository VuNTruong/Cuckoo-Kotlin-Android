package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetNotificationsForUserService
import com.beta.myhbt_api.Controller.GetOrderInCollectionOfLatestNotificationService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Interfaces.LoadMorePostsInterface
import com.beta.myhbt_api.Model.Notification
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterNotification
import kotlinx.android.synthetic.main.fragment_notification.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationFragment: Fragment(), LoadMorePostsInterface {
    // Array of notifications to be shown
    private var arrayOfNotifications = ArrayList<Notification>()

    // Adapter for the recycler view
    private var adapter: RecyclerViewAdapterNotification ?= null

    // User id of the currently logged in user
    private var currentUserId = ""

    // Current location in list of the user (use this so that server will know from where to load notifications)
    private var currentLocationInList = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the recycler view
        notificationView.layoutManager = LinearLayoutManager(this.activity)
        notificationView.itemAnimator = DefaultItemAnimator()

        // Update the adapter
        adapter = RecyclerViewAdapterNotification(arrayOfNotifications, this.requireActivity(), this)

        // Add adapter to the RecyclerView
        notificationView.adapter = adapter

        // Call the function to get info of the current user and start load notifications
        getInfoOfCurrentUserAndLoadNotifications()
    }

    //******************************* GET INFO OF CURRENT USER SEQUENCE *******************************
    // The function to get info of the currently logged in user and start loading notificaitions
    private fun getInfoOfCurrentUserAndLoadNotifications () {
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
                    currentUserId = userId

                    // Call the function to start loading notifications
                    loadOrderInCollectionOfLatestNotification()
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //******************************* END GET INFO OF CURRENT USER SEQUENCE *******************************

    //******************************* GET NOTIFICATIONS SEQUENCE *******************************
    /*
    There are 2 things in this sequence
    1. Get order in collection of latest notification in collection
    2. Start loading notifications
     */

    // The function to load order in collection of latest notification in collection
    private fun loadOrderInCollectionOfLatestNotification () {
        // Create the get order in collection of latest notification service
        val getOrderInCollectionOfLatestNotificationService: GetOrderInCollectionOfLatestNotificationService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
            GetOrderInCollectionOfLatestNotificationService::class.java)

        // Create the call object to perform the call
        val call: Call<Any> = getOrderInCollectionOfLatestNotificationService.getOrderInCollectionOfLatestNotification()

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

                    // Get data from the response body (order in collection of latest notification)
                    val data = (responseBody["data"] as Double).toInt()

                    // Update current location in list as order in collection of latest notification
                    currentLocationInList = data

                    // Call the function to start loading notifications
                    loadNotificationsForUser()
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to load notifications for user
    private fun loadNotificationsForUser () {
        // Create the get notification for user service
        val getNotificationForUserService: GetNotificationsForUserService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
            GetNotificationsForUserService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getNotificationForUserService.getNotificationsForUser(currentUserId, currentLocationInList)

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

                    // Get new current location in list for next load
                    val newCurrentLocationInList = (responseBody["newCurrentLocationInList"] as Double).toInt()

                    // Get data from the response body (list of notifications)
                    val data = responseBody["data"] as ArrayList<Notification>

                    // Update the array of notifications
                    arrayOfNotifications.addAll(data)

                    // Update current location in list for the user
                    currentLocationInList = newCurrentLocationInList

                    // Reload the Recycler view
                    notificationView.adapter!!.notifyDataSetChanged()
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //******************************* END GET NOTIFICATIONS SEQUENCE *******************************

    override fun loadMorePosts() {
        // Call the function to load more notifications
        loadNotificationsForUser()
    }
}