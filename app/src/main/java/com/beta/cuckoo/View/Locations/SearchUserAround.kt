package com.beta.cuckoo.View.Locations

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.LocationRepositories.LocationRepository
import com.beta.cuckoo.Repository.UserRepositories.FollowRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterSearchUser
import kotlinx.android.synthetic.main.activity_post_recommend.*
import kotlinx.android.synthetic.main.activity_search_user_around.*
import kotlinx.android.synthetic.main.activity_user_search.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SearchUserAround : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Location repository
    private lateinit var locationRepository: LocationRepository

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

        // Hide the navigation bar
        supportActionBar!!.hide()

        // Set up on click listener for the back button
        backButtonUserSearch.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Instantiate follow repository
        followRepository = FollowRepository(executorService, applicationContext)

        // Instantiate location repository
        locationRepository = LocationRepository(executorService, applicationContext)

        // Instantiate the recycler view
        searchUserView.layoutManager = LinearLayoutManager(applicationContext)
        searchUserView.itemAnimator = DefaultItemAnimator()

        // Call the function to get info of the currently logged in user and load list of users around
        searchUserAround("")

        // Add text watcher to the search text edit
        searchUserEditText.addTextChangedListener(textWatcher)
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
            adapter = RecyclerViewAdapterSearchUser(users, this, followRepository)

            // Add adapter to the RecyclerView
            searchUserView.adapter = adapter
        }
    }
    //************************************** END GET LIST OF USERS AROUND SEQUENCE **************************************
}