package com.beta.cuckoo.View.Chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Interfaces.ChatMessageOptionsInterface
import com.beta.cuckoo.Model.MessagePhoto
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.MessageRepositories.MessageRepository
import com.beta.cuckoo.Repository.UserRepositories.UserBlockRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.Repository.UserRepositories.UserTrustRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterMessageOption
import com.beta.cuckoo.View.Menus.TrustModeLearnMoreMenu
import kotlinx.android.synthetic.main.activity_chat_message_options.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChatMessageOptions : AppCompatActivity(), ChatMessageOptionsInterface {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Chat room id in which the 2 users are in
    private lateinit var chatRoomId: String

    // User id of the message receiver
    private lateinit var messageReceiverUserId: String

    // Adapter for the recycler view
    private lateinit var adapter: RecyclerViewAdapterMessageOption

    // Array of photo URLs of chat room
    private var arrayOfPhotoURLsOfChatRoom = ArrayList<MessagePhoto>()

    // The message repository
    private lateinit var messageRepository: MessageRepository

    // The user repository
    private lateinit var userRepository: UserRepository

    // The user block repository
    private lateinit var userBlockRepository: UserBlockRepository

    // The user trust repository
    private lateinit var userTrustRepository: UserTrustRepository

    // The message repository
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

        // Instantiate user trust repository
        userTrustRepository = UserTrustRepository(executorService, applicationContext)

        // Instantiate the recycler view
        messageOptionsView.layoutManager = LinearLayoutManager(applicationContext)
        messageOptionsView.itemAnimator = DefaultItemAnimator()

        // Call the function to check for trust between the 2 users
        checkTrustStatusBetween2UsersAndAllowScreenShot(messageReceiverUserId)

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
            adapter = RecyclerViewAdapterMessageOption(messageReceiverUserId, chatRoomId, this, userRepository, userBlockRepository, userTrustRepository, messageRepository, this, arrayOfPhotoURLsOfChatRoom)

            // Add adapter to the Recycler View
            messageOptionsView.adapter = adapter
        }
    }

    //************************* CHECK FOR USER'S TRUST *************************
    // The function to check and see if current user trusts user chatting with or not
    private fun checkTrustStatusBetween2UsersAndAllowScreenShot (otherUserId: String) {
        // If current user is not trusted by user chatting with, don't let the user take screenshot
        userTrustRepository.checkTrustStatusBetweenOtherUserAndCurrentUser(otherUserId) {isTrusted ->
            if (!isTrusted) {
                // Prevent user from taking screenshot
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
    //************************* CHECK FOR USER'S TRUST *************************

    // The function to open learn more menu
    override fun openLearnMore () {
        // The bottom sheet object
        val bottomSheet = TrustModeLearnMoreMenu()

        // Show the menu
        bottomSheet.show(supportFragmentManager, "TAG")
    }
}