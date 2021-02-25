package com.beta.cuckoo.View.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.FollowRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterSearchUser
import com.beta.cuckoo.ViewModel.UserViewModel
import kotlinx.android.synthetic.main.fragment_search_friends.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SearchFriendFragment : Fragment() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // User view model
    private lateinit var userViewModel : UserViewModel

    // Follow repository
    private lateinit var followRepository: FollowRepository

    // Array of users
    private var users = ArrayList<User>()

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterSearchUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate follow repository
        followRepository = FollowRepository(executorService, this.requireContext())

        // Instantiate the user view model
        userViewModel = UserViewModel(this.requireContext())

        // Instantiate the recycler view
        searchFriendsView.layoutManager = LinearLayoutManager(this.requireContext())
        searchFriendsView.itemAnimator = DefaultItemAnimator()

        // Add text watcher to the search text edit
        searchFriendEditText.addTextChangedListener(textWatcher)

        // Call the function to initiate the list of friends search
        searchUsers("")
    }

    // The text watcher which will take action of when there is change in search text edit
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Call the function search for user based on current search query
            searchUsers(s.toString())
        }
    }

    // The function to load list of users based on search query
    fun searchUsers (searchQuery: String) {
        // Call the function to get list of found users based on search query
        userViewModel.searchUserBasedOnFullName(searchQuery) {listOfUsers ->
            // Update list of users
            users = listOfUsers

            // Update the adapter
            adapter = RecyclerViewAdapterSearchUser(users, this@SearchFriendFragment.requireActivity(), followRepository)

            // Add adapter to the RecyclerView
            searchFriendsView.adapter = adapter
        }
    }
}