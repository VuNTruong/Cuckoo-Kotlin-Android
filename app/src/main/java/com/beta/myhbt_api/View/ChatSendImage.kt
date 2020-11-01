package com.beta.myhbt_api.View

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.MimeTypeMap
import com.beta.myhbt_api.Controller.CreateNewChatMessagePhotoService
import com.beta.myhbt_api.Controller.CreateNewMessageService
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_chat_send_image.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.floor

class ChatSendImage : AppCompatActivity() {
    // Image Uri of the selected image
    private var imageURI: Uri? = null

    // User id of the message receiver
    private var messageReceiverUserId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_send_image)

        // Get user id of the message receiver from previous activity
        messageReceiverUserId = intent.getStringExtra("messageReceiverUserId")!!

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
            getUserInfoAndSendMessage(messageReceiverUserId)
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

    // The function to get info of currently logged in user and create new message based on that
    private fun getUserInfoAndSendMessage (receiverUserId: String) {
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

                    // Execute the AsyncTask to create new message
                    sendMessage(userId, receiverUserId)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to create new message sent by current user and include the image
    fun sendMessage (userId: String, receiverUserId: String) {
        // Create the create new messages service
        val createNewMessageService: CreateNewMessageService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            CreateNewMessageService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = createNewMessageService.createNewMessage(userId, receiverUserId, "image")

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that message is created
                if (response.body() != null) {
                    // Body of the response
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get id of the sent message
                    val sentMessageId = data["_id"] as String

                    // Call the function to upload image for the message
                    fileUploader(imageURI!!, sentMessageId)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to perform the file uploading procedure
    private fun fileUploader(imageURI: Uri, messageId: String) {
        // Generate name for the image
        val imageName = generateRandomString(20)

        // Create the storage reference
        val storageReference =
            FirebaseStorage.getInstance().getReference("messagePhotos")

        // Put name for the image
        val reference = storageReference.child("${imageName}.${getExtension(imageURI)}")

        // Start the upload task. This is the uploadTask which will be used to keep track of the upload process
        val uploadTask = reference.putFile(imageURI)

        // When uploading is done, get URL of that image
        uploadTask.addOnSuccessListener {
            // Get URL of the image that has just been uploaded to the storage
            reference.downloadUrl.addOnSuccessListener { uri ->
                // Call the function to add new message photo URL to the database
                createNewImageURL(uri.toString(), messageId)
            }
        }
    }

    // The function to create new chat image URL in the database
    private fun createNewImageURL (imageURL: String, messageId: String) {
        // Create the create chat message photo service
        val createMessagePhotoService: CreateNewChatMessagePhotoService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            CreateNewChatMessagePhotoService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = createMessagePhotoService.createMessagePhoto(messageId, imageURL)

        // Perform the API call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {

            }
        })
    }

    // The function to get extension of the image
    private fun getExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    // The function to generate a random string
    private fun generateRandomString (length: Int): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var randomString = ""
        for (i in 0..length) {
            randomString += chars[floor(Math.random() * chars.length).toInt()]
        }
        return randomString
    }
}
