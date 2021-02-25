package com.beta.cuckoo.View.Chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.MessageRepositories.MessageRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterSearchUserToChatWith
import com.beta.cuckoo.ViewModel.UserViewModel
import kotlinx.android.synthetic.main.activity_search_user_to_chat_with.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SearchUserToChatWith : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // User view model
    private lateinit var userViewModel: UserViewModel

    // Message repository
    private lateinit var messageRepository: MessageRepository

    // Array of users
    private var users = ArrayList<User>()

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterSearchUserToChatWith

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user_to_chat_with)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate message repository
        messageRepository = MessageRepository(executorService, applicationContext)

        // Instantiate user view model
        userViewModel = UserViewModel(applicationContext)

        // Set on click listener for the back button
        backButtonSearchUserToChatWith.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Instantiate the recycler view
        searchUserToChatView.layoutManager = LinearLayoutManager(applicationContext)
        searchUserToChatView.itemAnimator = DefaultItemAnimator()

        // Add text watcher to the search text edit
        searchUserToChatEditText.addTextChangedListener(textWatcher)
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
        // Call the function to search user based on full name
        userViewModel.searchUserBasedOnFullName(searchQuery) {listOfUsers ->
            // Update list of users
            users = listOfUsers

            // Update the adapter
            adapter = RecyclerViewAdapterSearchUserToChatWith(users, this@SearchUserToChatWith, messageRepository)

            // Add adapter to the RecyclerView
            searchUserToChatView.adapter = adapter
        }
    }
}
