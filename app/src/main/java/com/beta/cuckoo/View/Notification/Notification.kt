package com.beta.cuckoo.View.Notification

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Interfaces.LoadMorePostsInterface
import com.beta.cuckoo.Model.Notification
import com.beta.cuckoo.R
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterNotification
import com.beta.cuckoo.ViewModel.NotificationViewModel
import kotlinx.android.synthetic.main.activity_notification.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Notification : AppCompatActivity(), LoadMorePostsInterface {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // The notification view model
    private lateinit var notificationViewModel: NotificationViewModel

    // Array of notifications to be shown
    private var arrayOfNotifications = ArrayList<Notification>()

    // Adapter for the recycler view
    private var adapter: RecyclerViewAdapterNotification?= null

    // Current location in list of the user (use this so that server will know from where to load notifications)
    private var currentLocationInList = 0

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        // Hide the navigation bar
        supportActionBar!!.hide()

        // Set up on click listener for the back button
        backButtonNotificationCenter.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Instantiate the notification view model
        notificationViewModel = NotificationViewModel(applicationContext)

        // Instantiate the recycler view
        notificationView.layoutManager = LinearLayoutManager(this)
        notificationView.itemAnimator = DefaultItemAnimator()

        // Update the adapter
        adapter = RecyclerViewAdapterNotification(arrayOfNotifications, this, this, executorService)

        // Add adapter to the RecyclerView
        notificationView.adapter = adapter

        // Call the function to get info of the current user and start load notifications
        loadNotificationsForCurrentUser()
    }

    //******************************* GET INFO OF CURRENT USER SEQUENCE *******************************
    // The function to start loading notifications for the currently logged in user
    private fun loadNotificationsForCurrentUser () {
        // Call the function to load notifications for the currently logged in user
        notificationViewModel.loadNotificationsForFirstTime { listOfNotifications, newCurrentLocationInList ->
            // Update the array of notifications
            arrayOfNotifications.addAll(listOfNotifications)

            // Update current location in list for the user
            currentLocationInList = newCurrentLocationInList

            // Reload the Recycler view
            notificationView.adapter!!.notifyDataSetChanged()
        }
    }

    // The function to load more notifications for the user
    private fun loadMoreNotifications () {
        // Call the function to load more notifications for the user
        notificationViewModel.loadMoreNotifications(currentLocationInList) {listOfNotifications, newCurrentLocationInList ->
            // Update the array of notifications
            arrayOfNotifications.addAll(listOfNotifications)

            // Update current location in list for the user
            currentLocationInList = newCurrentLocationInList

            // Reload the Recycler view
            notificationView.adapter!!.notifyDataSetChanged()
        }
    }

    override fun loadMorePosts() {
        // Call the function to load more notifications
        loadMoreNotifications()
    }
}