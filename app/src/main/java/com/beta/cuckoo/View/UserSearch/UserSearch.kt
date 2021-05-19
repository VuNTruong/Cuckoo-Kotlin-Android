package com.beta.cuckoo.View.UserSearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.FollowRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterSearchUser
import com.beta.cuckoo.ViewModel.UserViewModel
import kotlinx.android.synthetic.main.activity_user_search.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UserSearch : AppCompatActivity() {
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

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_search)

        // The the action bar
        supportActionBar!!.hide()

        // Instantiate follow repository
        followRepository = FollowRepository(executorService, applicationContext)

        // Instantiate the user view model
        userViewModel = UserViewModel(applicationContext)

        // Instantiate the recycler view
        searchUserView.layoutManager = LinearLayoutManager(applicationContext)
        searchUserView.itemAnimator = DefaultItemAnimator()

        // Add text watcher to the search text edit
        searchUserEditText.addTextChangedListener(textWatcher)

        // Call the function to initiate the list of friends search
        searchUsers("")

        // Set up on click listener for the back button
        backButtonUserSearch.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }
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
            adapter = RecyclerViewAdapterSearchUser(users, this, followRepository)

            // Add adapter to the RecyclerView
            searchUserView.adapter = adapter
        }
    }
}