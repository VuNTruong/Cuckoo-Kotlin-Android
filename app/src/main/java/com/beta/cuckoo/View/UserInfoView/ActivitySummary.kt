package com.beta.cuckoo.View.UserInfoView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterUserStats
import com.beta.cuckoo.ViewModel.UserStatsViewModel
import kotlinx.android.synthetic.main.activity_summary.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ActivitySummary : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // User repository
    private lateinit var userRepository: UserRepository

    // User stats view model
    private lateinit var userStatsViewModel: UserStatsViewModel

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterUserStats

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // Show the action bar
        supportActionBar!!.hide()

        // Set up on click listener for the back button
        backButtonActivitySummary.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Instantiate user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate user stats view model
        userStatsViewModel = UserStatsViewModel(applicationContext)

        // Instantiate the recycler view
        userStatsView.layoutManager = LinearLayoutManager(this)
        userStatsView.itemAnimator = DefaultItemAnimator()

        // Call the function to get info of the currently logged in user and load user account stats
        loadAccountStats()
    }

    //************************ LOAD ACCOUNT STATS SEQUENCE ************************
    // The function to load account stats for the currently logged in user
    private fun loadAccountStats () {
        userStatsViewModel.getCurrentUserAccountStats {arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit ->
            // Update the adapter
            adapter = RecyclerViewAdapterUserStats(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this, userRepository)

            // Add adapter to the RecyclerView
            userStatsView.adapter = adapter
        }
    }
}