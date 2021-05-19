package com.beta.cuckoo.View.Chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.MessageRoom
import com.beta.cuckoo.R
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterMessageRoom
import com.beta.cuckoo.ViewModel.MessageViewModel
import kotlinx.android.synthetic.main.activity_chat_main_menu.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChatMainMenu : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Message repository
    private lateinit var messageRepository: MessageViewModel

    // Array of message room
    private var arrayOfMessageRooms = ArrayList<MessageRoom>()

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterMessageRoom

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_main_menu)

        // Hide the navigation bar
        supportActionBar!!.hide()

        // Set up on click listener for the back button
        backButtonChatMainMenu.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Instantiate the notification repository
        messageRepository = MessageViewModel(applicationContext)

        // Instantiate the recycler view
        messageRoomView.layoutManager = LinearLayoutManager(this)
        messageRoomView.itemAnimator = DefaultItemAnimator()

        // Set up on click listener for the create new message button
        createNewMessageButton.setOnClickListener {
            // Take user to the activity where the user can search for user and send message to that one
            val intent = Intent(this, SearchUserToChatWith::class.java)
            startActivity(intent)
        }

        // Call the function to get info of the currently logged in user which will then call the function
        // to get message rooms in which the current user is in
        getMessageRoomsOfCurrentUser()
    }

    // The function to get list of message rooms of the currently logged in user
    private fun getMessageRoomsOfCurrentUser () {
        // Call the function to get list of message rooms for the currently logged in user\
        messageRepository.getMessageRoomsOfUser { messageRooms ->
            // Update the array of message rooms
            arrayOfMessageRooms = messageRooms

            // Update the adapter
            adapter = RecyclerViewAdapterMessageRoom(arrayOfMessageRooms, this, executorService)

            // Add adapter to the RecyclerView
            messageRoomView.adapter = adapter
        }
    }
}