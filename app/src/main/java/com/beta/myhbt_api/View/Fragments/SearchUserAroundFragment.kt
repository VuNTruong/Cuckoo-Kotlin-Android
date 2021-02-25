package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.Repository.LocationRepositories.LocationRepository
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterSearchFriend
import com.beta.myhbt_api.ViewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_search_friends.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SearchUserAroundFragment : Fragment() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Location repository
    private lateinit var locationRepository: LocationRepository

    // Array of users
    private var users = ArrayList<User>()

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterSearchFriend

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate location repository
        locationRepository = LocationRepository(executorService, this.requireContext())

        // Instantiate the recycler view
        searchFriendsView.layoutManager = LinearLayoutManager(this.requireContext())
        searchFriendsView.itemAnimator = DefaultItemAnimator()

        // Call the function to get info of the currently logged in user and load list of users around
        searchUserAround("")

        // Add text watcher to the search text edit
        searchFriendEditText.addTextChangedListener(textWatcher)
    }

    // The text watcher which will take action of when there is change in search text edit
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Call the function search for user based on current search query
            searchUserAround(s.toString())
        }
    }

    //************************************** GET INFO OF CURRENTLY LOGGED IN USER SEQUENCE **************************************
    // The function to search for users around last updated location of the currently logged in user
    fun searchUserAround (searchQuery: String) {
        // Call the function to get list of users around last updated location of the currently logged in user
        locationRepository.searchUserAround(searchQuery) {listOfUsers ->
            // Update list of users
            users = listOfUsers

            // Update the adapter
            adapter = RecyclerViewAdapterSearchFriend(users, this.requireActivity())

            // Add adapter to the RecyclerView
            searchFriendsView.adapter = adapter
        }
    }
    //************************************** END GET LIST OF USERS AROUND SEQUENCE **************************************
}