package com.beta.myhbt_api.ViewModel

import android.content.Context
import com.beta.myhbt_api.Model.Notification
import com.beta.myhbt_api.Repository.NotificationRepositories.NotificationRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NotificationViewModel (context: Context) {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Repository for the notification
    private val notificationRepository: NotificationRepository = NotificationRepository(executorService, context)

    // The function to get notification for the first time when view is instantiated
    fun loadNotificationsForFirstTime (callback: (listOfNotifications: ArrayList<Notification>, newCurrentLocationInList: Int) -> Unit) {
        // Call the function to get order in collection of latest notification in the database
        notificationRepository.getOrderInCollectionOfLatestNotification { orderInCollectionOfLatestNotification ->
            // Call the function to get notifications for the user
            notificationRepository.getNotificationsForCurrentUser(orderInCollectionOfLatestNotification) {newCurrentLocationInList, arrayOfNotifications ->
                // Call the callback function to return new order in collection and array of notifications
                callback(arrayOfNotifications, newCurrentLocationInList)
            }
        }
    }

    // The function to load more notifications
    fun loadMoreNotifications (currentLocationInList: Int, callback: (listOfNotifications: ArrayList<Notification>, newCurrentLocationInList: Int) -> Unit) {
        // Call the function to load more notifications from the specified current location in list
        notificationRepository.getNotificationsForCurrentUser(currentLocationInList) {newCurrentLocationInList, arrayOfNotifications ->
            // Call the callback function to return new order in collection and array of notifications
            callback(arrayOfNotifications, newCurrentLocationInList)
        }
    }
}