package com.beta.cuckoo.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterUserStats
import com.beta.cuckoo.ViewModel.UserStatsViewModel
import kotlinx.android.synthetic.main.fragment_user_stats.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UserStatsFragment : Fragment() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // User repository
    private lateinit var userRepository: UserRepository

    // User stats view model
    private lateinit var userStatsViewModel: UserStatsViewModel

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterUserStats

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate user repository
        userRepository = UserRepository(executorService, this.requireContext())

        // Instantiate user stats view model
        userStatsViewModel = UserStatsViewModel(this.requireContext())

        // Instantiate the recycler view
        userStatsView.layoutManager = LinearLayoutManager(this.requireActivity())
        userStatsView.itemAnimator = DefaultItemAnimator()

        // Call the function to get info of the currently logged in user and load user account stats
        loadAccountStats()
    }

    //************************ LOAD ACCOUNT STATS SEQUENCE ************************
    // The function to load account stats for the currently logged in user
    private fun loadAccountStats () {
        userStatsViewModel.getCurrentUserAccountStats {arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit ->
            // Update the adapter
            adapter = RecyclerViewAdapterUserStats(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsFragment.requireActivity(), userRepository)

            // Add adapter to the RecyclerView
            userStatsView.adapter = adapter
        }
    }
}