package com.beta.cuckoo.View.PostDetail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.beta.cuckoo.Network.LikesAndComments.CreateNewPostCommentPhotoService
import com.beta.cuckoo.Network.LikesAndComments.CreateNewPostCommentService
import com.beta.cuckoo.Network.User.GetCurrentlyLoggedInUserInfoService
import com.beta.cuckoo.Network.RetrofitClientInstance
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.PostRepositories.PostCommentRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.Utils.AdditionalAssets
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_hbtgram_post_detail_comment_send_image.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.floor

class PostDetailCommentSendImage : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Post comment repository
    private lateinit var postCommentRepository: PostCommentRepository

    // User repository
    private lateinit var userRepository: UserRepository

    // Additional assets
    private lateinit var additionalAssets: AdditionalAssets

    // Instance of the FirebaseStorage
    private val storage = FirebaseStorage.getInstance()

    // These objects are used for socket.io
    //private lateinit var mSocket: Socket
    private val gson = Gson()

    // Image Uri of the selected image
    private var imageURI: Uri? = null

    // Post id of the post currently work with
    private var postId = ""

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hbtgram_post_detail_comment_send_image)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate additional assets
        additionalAssets = AdditionalAssets(applicationContext)

        // Instantiate the post comment repository
        postCommentRepository = PostCommentRepository(executorService, applicationContext)

        // Instantiate the user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Set on click listener for the back button
        backButtonPostDetailCommentSendImage.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Get post id of the post currently working with from the previous activity
        postId = intent.getStringExtra("postId")!!

        // Call the function to set up socket.io
        setUpSocket()

        // Call the function to open file chooser at beginning the activity launch
        fileChooser()

        // Add event listener for the choose photo button
        chooseOtherCommentImageToSendButton.setOnClickListener{
            // Call the function to open the file chooser so that user can chooser other image to send
            fileChooser()
        }

        // Add event listener for the send image button
        sendCommentImageButton.setOnClickListener {
            // Call the function to send image for the comment
            createCommentWithPhoto()
        }
    }

    //************************ DO THINGS WITH THE SOCKET.IO ************************
    // The function to start setting up socket.io
    private fun setUpSocket () {
        // Bring user into the chat room between this user and the selected user
        MainMenu.mSocket.emit("jumpInPostDetailRoom", gson.toJson(hashMapOf(
            "postId" to postId
        )))
    }
    //************************ END WORKING WITH SOCKET.IO ************************

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
            previewCommentImageToSend.setImageURI(imageURI)
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

                    // After that, finish this activity
                    this@PostDetailCommentSendImage.finish()
                }
            }
        }
    }
    //********************************************* END SEND IMAGE SEQUENCE *********************************************
}
