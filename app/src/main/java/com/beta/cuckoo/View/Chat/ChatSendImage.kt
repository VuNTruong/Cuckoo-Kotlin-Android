package com.beta.cuckoo.View.Chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.MessageRepositories.MessageRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.Utils.AdditionalAssets
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.beta.cuckoo.ViewModel.MessageViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_chat_send_image.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.floor

class ChatSendImage : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Additional assets
    private lateinit var additionalAssets: AdditionalAssets

    // Message view model
    private lateinit var messageViewModel: MessageViewModel

    // Message repository
    private lateinit var messageRepository: MessageRepository

    // User repository
    private lateinit var userRepository: UserRepository

    // These objects are used for socket.io
    private val gson = Gson()

    // Image Uri of the selected image
    private var imageURI: Uri? = null

    // User id of the message receiver
    private var messageReceiverUserId = ""

    // Chat room id between the currently logged in user and user currently chatting with
    private var chatRoomId = ""

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_send_image)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate additional assets
        additionalAssets = AdditionalAssets(applicationContext)

        // Set on click listener for the back button
        backButtonChatSendImage.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Initialize message repository
        messageRepository = MessageRepository(executorService, applicationContext)

        // Initialize user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Initialize message view model
        messageViewModel = MessageViewModel(applicationContext)

        // Get user id of the message receiver from previous activity
        messageReceiverUserId = intent.getStringExtra("messageReceiverUserId")!!

        // Get chat room id from the previous activity
        chatRoomId = intent.getStringExtra("chatRoomId")!!

        //************************ DO THINGS WITH THE SOCKET.IO ************************
        // Bring user into the chat room between this user and the selected user
        MainMenu.mSocket.emit("jumpInChatRoom", gson.toJson(hashMapOf(
            "chatRoomId" to chatRoomId
        )))

        //************************ END WORKING WITH SOCKET.IO ************************

        // Open file chooser at beginning
        fileChooser()

        // Set on click listener for the choose another photo button
        chooseOtherImageToSendButton.setOnClickListener {
            // Call the function to open the file chooser again
            fileChooser()
        }

        // Set on click listener for the send message button
        sendImageButton.setOnClickListener {
            // Call the function to get info of the current user and send the image
            //getUserInfoAndSendMessage(messageReceiverUserId)
            uploadImageAndSendMessage(imageURI!!)
        }
    }

    // The function to open file chooser to get the image
    private fun fileChooser() {
        // Create the new intent in order to come to the file chooser
        val intent = Intent()

        // Set the intent to just pick the image
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        // Star the activity to get the image
        startActivityForResult(intent, 0)
    }

    // The function to load newly picked image into the array of selected images
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Add the selected image's Uri into the array of selected images
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageURI = data.data
            previewImageToSend.setImageURI(imageURI)
        }
    }

    // The function to perform the file uploading procedure
    private fun uploadImageAndSendMessage(imageURI: Uri) {
        // Call the function to upload image to the storage and get download URL of the uploaded photo
        additionalAssets.uploadImageToStorage(imageURI, "messagePhotos") { imageUploaded, imageURL ->
            if (imageUploaded) {
                // Call the function to add new message photo URL to the database
                sendMessage(imageURL)
            }
        }
    }

    // The function to create new message sent by current user and include the image
    private fun sendMessage (imageURL: String) {
        // Call the function to send message
        messageRepository.sendMessage(chatRoomId, messageReceiverUserId, "image") {messageSentFirstTime, messageObject, chatRoomId, currentUserId, messageSentStatus ->
            // Check the message sent status
            if (messageSentStatus == "Not sent. Is blocking receiver") {
                // Show toast and let user know that user is blocking receiver
                Toast.makeText(applicationContext, "You are blocking receiver", Toast.LENGTH_SHORT).show()
            } else if (messageSentStatus == "Not sent. Is being blocked by receiver") {
                // Show toast and let user know that user is being blocked by receiver
                Toast.makeText(applicationContext, "Message cannot be sent at this time", Toast.LENGTH_SHORT).show()
            } else {
                if (!messageSentFirstTime) {
                    // Call the function to create new message image
                    messageRepository.createNewMessageImage(messageObject.getMessageId(), imageURL) {
                        // After photo is sent to the storage and image URL is sent to the database, emit event to the server so that
                        // the server know that this user has sent an image as message
                        MainMenu.mSocket.emit("userSentPhotoAsMessage", gson.toJson(hashMapOf(
                            "sender" to currentUserId,
                            "receiver" to messageReceiverUserId,
                            "content" to "image",
                            "messageId" to messageObject.getMessageId(),
                            "chatRoomId" to chatRoomId
                        )))

                        // After that, finish this activity
                        this@ChatSendImage.finish()
                    }
                }
            }
        }
    }
}
