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
import com.beta.cuckoo.Repository.PostRepositories.PostCommentRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.Utils.AdditionalAssets
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PostDetailSendImageMenu (parentActivity: Activity, postId: String) : BottomSheetDialogFragment() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Post comment repository
    private lateinit var postCommentRepository: PostCommentRepository

    // User repository
    private lateinit var userRepository: UserRepository

    // Additional assets
    private lateinit var additionalAssets: AdditionalAssets

    // Post id of post which will take the comment
    private val postId = postId

    // Image Uri of the selected image
    private var imageURI: Uri? = null

    // Parent activity
    private val parentActivity = parentActivity

    // These objects are used for socket.io
    private val gson = Gson()

    //***************************** COMPONENTS FROM THE LAYOUT *****************************
    // The send image button
    private lateinit var sendImageButton: Button

    // The choose image button
    private lateinit var chooseImageButton: Button

    // Image view which will be used to preview image to be sent
    private lateinit var imageToSend: ImageView

    // The sending layout
    private lateinit var sendingLayout: ConstraintLayout
    //***************************** END COMPONENTS FROM THE LAYOUT *****************************

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // The view object
        val view = inflater.inflate(R.layout.pick_image_to_send_menu_item_post_detail, container, false)

        //***************************** INSTANTIATE COMPONENTS FROM THE LAYOUT *****************************
        // Instantiate the send button
        sendImageButton = view.findViewById(R.id.sendImageButtonPostDetail)

        // Instantiate the choose image button
        chooseImageButton = view.findViewById(R.id.galleryImageButtonPostDetail)

        // Instantiate the image view which will be used to preview image to be sent
        imageToSend = view.findViewById(R.id.imageToSendPostDetail)

        // Instantiate the sending layout
        sendingLayout = view.findViewById(R.id.isSendingLayoutSendPostDetailImage)
        //***************************** END INSTANTIATE COMPONENTS FROM THE LAYOUT *****************************

        //***************************** SET ON CLICK LISTENER FOR BUTTONS IN LAYOUT *****************************
        // Set on click listener for the choose image button
        chooseImageButton.setOnClickListener {
            // Call the function which will let the user choose image to sent
            fileChooser()
        }

        // Set on click listener for the send image button
        sendImageButton.setOnClickListener {
            // If user has not selected an image, show toast and let the user know
            if (imageURI == null) {
                // Show toast to the user
                Toast.makeText(parentActivity, "Please select an image", Toast.LENGTH_SHORT).show()
            } // Otherwise, start sending the image
            else {
                // Show the is sending layout
                sendingLayout.visibility = View.VISIBLE

                // Call the function which will let the user send image
                createCommentWithPhoto()
            }
        }
        //***************************** END SET ON CLICK LISTENER FOR BUTTONS IN LAYOUT *****************************

        // Instantiate additional assets
        additionalAssets = AdditionalAssets(parentActivity)

        // Instantiate the post comment repository
        postCommentRepository = PostCommentRepository(executorService, parentActivity)

        // Instantiate the user repository
        userRepository = UserRepository(executorService, parentActivity)

        // Bring user into the chat room between this user and the selected user
        MainMenu.mSocket.emit("jumpInPostDetailRoom", gson.toJson(hashMapOf(
            "postId" to postId
        )))

        // Call the function to let user choose image to send initially
        fileChooser()

        // Hide the is sending layout initially
        sendingLayout.visibility = View.INVISIBLE

        // Return the view
        return view
    }

    //********************************************* IMAGE CHOOSING SEQUENCE *********************************************
    /*
    In this sequence, we will do 3 things
    1. Open the file chooser window
    2. Load chosen image into the ImageView
    3. Get extension of the chosen image
     */

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

        // Show selected image on the preview image view
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageURI = data.data
            imageToSend.setImageURI(imageURI)
        }
    }
    //********************************************* END IMAGE CHOOSING SEQUENCE *********************************************

    //********************************************* SEND IMAGE SEQUENCE *********************************************
    /*
    In this sequence, we will do 3 things
    1. Get info of the current user
    2. Send the comment to the comment collection in the database
    3. Upload the actual image of the comment to the storage
    4. Upload URL of the image to the comment photo collection in the database
     */

    // The function to get info of the current user
    private fun createCommentWithPhoto () {
        // Call the function to get info of the currently logged in user
        userRepository.getInfoOfCurrentUser { userObject ->
            // Call the function to create new comment in the comment collection of the database
            createNewComment("image", userObject.getId(), postId)
        }
    }

    // The function to create comment in the comment collection of the database
    // The function to create new comment for the post
    private fun createNewComment (commentContent: String, commentWriterUserId: String, postId: String) {
        // Call the function to create new comment
        postCommentRepository.createCommentForPost(commentContent, postId) {commentCreated, newCommentId ->
            if (commentCreated) {
                // Call the function to upload photo of the comment to the storage as well as upload
                // its URL to the database
                uploadImageToStorage(imageURI!!, newCommentId)
            }
        }
    }

    // The function to upload the image to the database
    private fun uploadImageToStorage (imageURI: Uri, commentId: String) {
        // Call the function to upload image to the database
        additionalAssets.uploadImageToStorage(imageURI, "hbtGramPostCommentPhotos") {imageUploaded, imageURL ->
            if (imageUploaded) {
                // Call the function to add new comment photo URL to the database
                createNewImageURL(imageURL, commentId)
            }
        }
    }

    // The function to upload the URL of the image to the comment photo collection in the database
    private fun createNewImageURL (imageURL: String, commentId: String) {
        // Call the function to create new comment image URL
        postCommentRepository.createNewCommentPhotoURL(imageURL, commentId) {urlCreated ->
            if (urlCreated) {
                // Call the function to get info of the currently logged in user
                userRepository.getInfoOfCurrentUser { userObject ->
                    // After photo is sent to the storage and image URL is sent to the database, emit event to the server so that
                    // the server know that this user has sent an image as message
                    MainMenu.mSocket.emit("imageSentAsComment", gson.toJson(hashMapOf(
                        "writer" to userObject.getId(),
                        "commentId" to commentId,
                        "content" to "image",
                        "postId" to postId
                    )))

                    // After that, hide the menu
                    this.dismiss()
                }
            }
        }
    }
    //********************************************* END SEND IMAGE SEQUENCE *********************************************
}