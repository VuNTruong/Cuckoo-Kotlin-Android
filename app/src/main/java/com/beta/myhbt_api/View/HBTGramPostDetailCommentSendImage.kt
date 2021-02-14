package com.beta.myhbt_api.View

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.beta.myhbt_api.Controller.LikesAndComments.CreateNewPostCommentPhotoService
import com.beta.myhbt_api.Controller.LikesAndComments.CreateNewPostCommentService
import com.beta.myhbt_api.Controller.User.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_hbtgram_post_detail_comment_send_image.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.floor

class HBTGramPostDetailCommentSendImage : AppCompatActivity() {
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
            getUserInfoAndCreateCommentWithPhoto()
        }
    }

    // The function to start setting up socket.io
    private fun setUpSocket () {
        //************************ DO THINGS WITH THE SOCKET.IO ************************
        // Bring user into the chat room between this user and the selected user
        MainMenu.mSocket.emit("jumpInPostDetailRoom", gson.toJson(hashMapOf(
            "postId" to postId
        )))
        //************************ END WORKING WITH SOCKET.IO ************************
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

        // Add the selected image's Uri into the array of selected images
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageURI = data.data
            previewCommentImageToSend.setImageURI(imageURI)
        }
    }

    // The function to get extension of the image
    private fun getExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
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
    private fun getUserInfoAndCreateCommentWithPhoto () {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetCurrentlyLoggedInUserInfoService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getCurrentlyLoggedInUserInfoService.getCurrentUserInfo()

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    val body = response.body()
                    print(body)
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user id of the currently logged in user
                    val userId = data["_id"] as String

                    // Call the function to create new comment in the comment collection of the database
                    createNewComment("image", userId, postId)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to create comment in the comment collection of the database
    // The function to create new comment for the post
    fun createNewComment (commentContent: String, commentWriterUserId: String, postId: String) {
        // Create the create comment service
        val postCommentService: CreateNewPostCommentService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            CreateNewPostCommentService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = postCommentService.createNewHBTGramPostComment(commentContent, commentWriterUserId, postId)

        // Perform the API call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is null, it means that comment can't be posted
                if (response.body() == null) {
                    // Show the alert
                    Toast.makeText(applicationContext, "Comment can't be posted", Toast.LENGTH_SHORT).show()
                } else {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data of the response from response body
                    val data = (responseBody["data"] as Map<String, Any>)["tour"] as Map<String, Any>

                    // Get id of the newly created comment
                    val newCommentId = data["_id"] as String

                    // Call the function to upload photo of the comment to the storage as well as upload
                    // its URL to the database
                    uploadImageToStorage(imageURI!!, newCommentId, commentWriterUserId)
                }
            }
        })
    }

    // The function to upload the image to the database
    fun uploadImageToStorage (imageURI: Uri, commentId: String, currentUserId: String) {
        // Generate name for the image
        val imageName = generateRandomString(20)

        // Create the storage reference
        val storageReference = storage.getReference("hbtGramPostCommentPhotos")

        // Put name for the image
        val reference = storageReference.child("${imageName}.${getExtension(imageURI)}")

        // Start the upload task. This is the uploadTask which will be used to keep track of the upload process
        val uploadTask = reference.putFile(imageURI)

        // When uploading is done, get URL of that image
        uploadTask.addOnSuccessListener {
            // Get URL of the image that has just been uploaded to the storage
            reference.downloadUrl.addOnSuccessListener { uri ->
                // Call the function to add new comment photo URL to the database
                createNewImageURL(uri.toString(), commentId, currentUserId)
            }
        }
    }

    // The function to upload the URL of the image to the comment photo collection in the database
    private fun createNewImageURL (imageURL: String, commentId: String, currentUserId: String) {
        // Create the create comment photo service
        val createCommentPhotoService: CreateNewPostCommentPhotoService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            CreateNewPostCommentPhotoService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = createCommentPhotoService.createNewHBTGramPostCommentPhoto(commentId, imageURL)

        // Perform the API call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // After photo is sent to the storage and image URL is sent to the database, emit event to the server so that
                // the server know that this user has sent an image as message
                MainMenu.mSocket.emit("imageSentAsComment", gson.toJson(hashMapOf(
                    "writer" to currentUserId,
                    "commentId" to commentId,
                    "content" to "image",
                    "postId" to postId
                )))

                // After that, finish this activity
                this@HBTGramPostDetailCommentSendImage.finish()
            }
        })
    }
    //********************************************* END SEND IMAGE SEQUENCE *********************************************

    // The function to create a random string of 20 characters
    private fun generateRandomString (length: Int): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var randomString = ""
        for (i in 0..length) {
            randomString += chars[floor(Math.random() * chars.length).toInt()]
        }
        return randomString
    }
}
