package com.beta.cuckoo.View.Menus

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.MessageRepositories.MessageRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.Utils.AdditionalAssets
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.beta.cuckoo.ViewModel.MessageViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChatSendImageMenu (parentActivity: Activity, messageReceiverUserId: String, chatRoomId: String) : BottomSheetDialogFragment() {
    // These objects are used for socket.io
    private val gson = Gson()

    // The parent activity
    private val parentActivity = parentActivity

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

    // User id of the message receiver
    private var messageReceiverUserId = messageReceiverUserId

    // Chat room id between the currently logged in user and user currently chatting with
    private var chatRoomId = chatRoomId

    // Image Uri of the selected image
    private var imageURI: Uri? = null

    // The send image button
    private lateinit var sendImageButton: Button

    // The choose image button
    private lateinit var chooseImageButton: Button

    // Image view which is used to preview image to be sent
    private lateinit var imageToSend: ImageView

    // The sending layout
    private lateinit var sendingLayout: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        // The view object
        val view = inflater.inflate(R.layout.pick_image_to_send_menu_item, container, false)

        //************************ COMPONENTS FROM THE MENU ************************
        // Instantiate the send image button
        sendImageButton = view.findViewById(R.id.sendImageButton)

        // Instantiate the choose image button
        chooseImageButton = view.findViewById(R.id.galleryImageButton)

        // Instantiate the image view which will be used to preview image to be sent
        imageToSend = view.findViewById(R.id.imageToSend)

        // Instantiate the sending layout
        sendingLayout = view.findViewById(R.id.isSendingLayoutSendMessagePhoto)
        //************************ COMPONENTS FROM THE MENU ************************

        // Initialize message repository
        messageRepository = MessageRepository(executorService, parentActivity)

        // Initialize user repository
        userRepository = UserRepository(executorService, parentActivity)

        // Initialize message view model
        messageViewModel = MessageViewModel(parentActivity)

        // Instantiate additional assets
        additionalAssets = AdditionalAssets(parentActivity)

        //************************ DO THINGS WITH THE SOCKET.IO ************************
        // Bring user into the chat room between this user and the selected user
        MainMenu.mSocket.emit("jumpInChatRoom", gson.toJson(hashMapOf(
            "chatRoomId" to chatRoomId
        )))
        //************************ END WORKING WITH SOCKET.IO ************************

        // Open file chooser at beginning
        fileChooser()

        // Set on click listener for the send image button
        sendImageButton.setOnClickListener {
            // If user has not selected image, show toast and let the user know that
            if (imageURI == null) {
                // Show toast to the user
                Toast.makeText(parentActivity, "Please select an image first", Toast.LENGTH_SHORT).show()
            } // Otherwise, start sending the image
            else {
                // Show the is sending layout
                sendingLayout.visibility = View.VISIBLE

                // Call the function to send selected image and upload selected image to the database as well
                uploadImageAndSendMessage(imageURI!!)
            }
        }

        // Set on click listener for the choose image button
        chooseImageButton.setOnClickListener {
            // Call the function to open the file chooser again
            fileChooser()
        }

        // Hide the is sending layout initially
        sendingLayout.visibility = View.INVISIBLE

        // Return the view
        return view
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
            imageToSend.setImageURI(imageURI)
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
                Toast.makeText(parentActivity, "You are blocking receiver", Toast.LENGTH_SHORT).show()
            } else if (messageSentStatus == "Not sent. Is being blocked by receiver") {
                // Show toast and let user know that user is being blocked by receiver
                Toast.makeText(parentActivity, "Message cannot be sent at this time", Toast.LENGTH_SHORT).show()
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

                        // After that, close the menu
                        this.dismiss()
                    }
                }
            }
        }
    }
}