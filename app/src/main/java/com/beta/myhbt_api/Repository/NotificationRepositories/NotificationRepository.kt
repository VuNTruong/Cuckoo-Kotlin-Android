package com.beta.myhbt_api.Repository.NotificationRepositories

import android.content.Context
import com.beta.myhbt_api.Network.Notifications.CreateNotificationService
import com.beta.myhbt_api.Network.Notifications.GetNotificationsForUserService
import com.beta.myhbt_api.Network.Notifications.GetOrderInCollectionOfLatestNotificationService
import com.beta.myhbt_api.Network.RetrofitClientInstance
import com.beta.myhbt_api.Model.Notification
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class NotificationRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The user repository (to get info of the currently logged in user for some uses)
    private val userRepository = UserRepository(executor, context)

    // The function to send notification
    fun sendNotification (content: String, forUser: String, fromUser: String, image: String, postId: String) {
        executor.execute {
            // Create the create notification service
            val createNotificationService: CreateNotificationService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
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
    }

    // The function to get order in collection of latest notification in the database
    fun getOrderInCollectionOfLatestNotification (callback: (orderInCollectionOfLatestNotification: Int) -> Unit) {
        executor.execute {
            // Create the get order in collection of latest notification service
            val getOrderInCollectionOfLatestNotificationService: GetOrderInCollectionOfLatestNotificationService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
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
                        val newCurrentLocationInList = (responseBody["data"] as Double).toInt()

                        // Return new current location in list via callback function
                        callback(newCurrentLocationInList)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to get notifications for the currently logged in user
    fun getNotificationsForCurrentUser (currentLocationInList: Int, callback: (newCurrentLocationInList: Int, arrayOfNotifications: ArrayList<Notification>) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the get notification for user service
                val getNotificationForUserService: GetNotificationsForUserService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetNotificationsForUserService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = getNotificationForUserService.getNotificationsForUser(userObject.getId(), currentLocationInList)

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
                            val listOfNotifications = responseBody["data"] as ArrayList<Notification>

                            // Return array of notifications and new current location in list via callback function
                            callback(newCurrentLocationInList, listOfNotifications)
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }
}