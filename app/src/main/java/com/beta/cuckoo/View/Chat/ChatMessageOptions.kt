package com.beta.cuckoo.View.Chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.MessageRepositories.MessageRepository
import com.beta.cuckoo.Repository.UserRepositories.UserBlockRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterMessageOption
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat_message_options.*
import kotlinx.android.synthetic.main.message_option_header.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChatMessageOptions : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Chat room id in which the 2 users are in
    private lateinit var chatRoomId: String

    // User id of the message receiver
    private lateinit var messageReceiverUserId: String

    // Adapter for the recycler view
    private lateinit var adapter: RecyclerViewAdapterMessageOption

    // Array of photo URLs of chat room
    private var arrayOfPhotoURLsOfChatRoom = ArrayList<String>()

    // The message repository
    private lateinit var messageRepository: MessageRepository

    // The user repository
    private lateinit var userRepository: UserRepository

    // The user block repository
    private lateinit var userBlockRepository: UserBlockRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_message_options)

        // Hide the action bar
        supportActionBar!!.hide()

        // Set up on click listener for the back button
        backButtonChatMessageOptions.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Get chat room id from the previous activity
        chatRoomId = intent.getStringExtra("chatRoomId")!!

        // Get message receiver user id from previous activity
        messageReceiverUserId = intent.getStringExtra("messageReceiverUserId")!!

        // Instantiate message repository
        messageRepository = MessageRepository(executorService, applicationContext)

        // Instantiate user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate user block repository
        userBlockRepository = UserBlockRepository(executorService, applicationContext)

        // Instantiate the recycler view
        messageOptionsView.layoutManager = LinearLayoutManager(applicationContext)
        messageOptionsView.itemAnimator = DefaultItemAnimator()

        // Call the function to set up message options and load photo of chat room
        setUpMessageOptionsAndLoadPhotos()
    }

    // The function to set up message options and load photos of chat room
    private fun setUpMessageOptionsAndLoadPhotos () {
        // Call the function to get list of message photos of chat room with specified id
        messageRepository.getPhotosOfChatRoom(chatRoomId) {arrayOfPhotos ->
            // Update array of photo
            arrayOfPhotoURLsOfChatRoom = arrayOfPhotos

            // Update the Recycler View
            adapter = RecyclerViewAdapterMessageOption(messageReceiverUserId, chatRoomId, applicationContext, userRepository, userBlockRepository, arrayOfPhotoURLsOfChatRoom)

            // Add adapter to the Recycler View
            messageOptionsView.adapter = adapter
        }
    }
}