package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Controller.User.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.UserStats.GetBriefUserStatsService
import com.beta.myhbt_api.Model.UserCommentInteraction
import com.beta.myhbt_api.Model.UserInteraction
import com.beta.myhbt_api.Model.UserLikeInteraction
import com.beta.myhbt_api.Model.UserProfileVisit
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterUserStats
import com.beta.myhbt_api.ViewModel.UserStatsViewModel
import kotlinx.android.synthetic.main.fragment_user_stats.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserStatsFragment : Fragment() {
    // User stats view model
    private lateinit var userStatsViewModel: UserStatsViewModel

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterUserStats

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            adapter = RecyclerViewAdapterUserStats(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsFragment.requireActivity())

            // Add adapter to the RecyclerView
            userStatsView.adapter = adapter
        }
    }
}