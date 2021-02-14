package com.beta.myhbt_api.View

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.beta.myhbt_api.Controller.User.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Controller.User.UpdateUserInfoService
import com.beta.myhbt_api.R
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_update_cover_photo.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateCoverPhoto : AppCompatActivity() {
    // Image Uri of the selected image
    private var imageURI: Uri? = null

    // Instance of the FirebaseStorage
    private val storage = FirebaseStorage.getInstance()

    // Map of fields which is used in updating user info
    private var mapOfFields = HashMap<String, Any>()

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_cover_photo)

        // Hide the action bar
        supportActionBar!!.hide()

        // Set on click listener for the back button
        backButtonUpdateCoverPhoto.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Execute the AsyncTask to get info of the currently logged in user as well as load current cover photo for the user
        GetCurrentUserInfoTask().execute()

        // Set on click listener for the choose new cover photo button
        chooseNewCoverImageButton.setOnClickListener {
            // Call the function top open the file chooser
            fileChooser()
        }

        // Set on click listener for the update cover photo button
        updateCoverPhotoButton.setOnClickListener {
            // Execute the AsyncTask to update the cover photo
            FileUploadingTask().execute(hashMapOf(
                "studentId" to mapOfFields["studentId"] as String,
                "userId" to mapOfFields["userId"] as String
            ))
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

    // The function to load chosen image into the image view
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageURI = data.data
            userCoverPhotoUpdateCoverPhoto.setImageURI(imageURI)
        }
    }

    // AsyncTask to get info of the currently logged in user
    inner class GetCurrentUserInfoTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            // Create the validate token service
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
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get user id in the database
                        val userId = data["_id"] as String

                        // Get avatar URL of the user
                        val avatarURL = data["avatarURL"] as String

                        // Get cover photo URL of the user
                        val coverURL = data["coverURL"] as String

                        // Get phone number of the user
                        val phoneNumber = data["phoneNumber"] as String

                        // Get facebook of the user
                        val facebook = data["facebook"] as String

                        // Get instagram of the user
                        val instagram = data["instagram"] as String

                        // Get twitter of the user
                        val twitter = data["twitter"] as String

                        // Get zalo of the user
                        val zalo = data["zalo"] as String

                        // Build the map of fields
                        mapOfFields = hashMapOf(
                            "userId" to userId,
                            "avatarURL" to avatarURL,
                            "coverURL" to coverURL,
                            "phoneNumber" to phoneNumber,
                            "facebook" to facebook,
                            "instagram" to instagram,
                            "twitter" to twitter,
                            "zalo" to zalo
                        )

                        // Load current cover photo into the ImageView
                        Glide.with(applicationContext)
                            .load(coverURL)
                            .into(userCoverPhotoUpdateCoverPhoto)
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to perform file uploading procedure
    inner class FileUploadingTask: AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get student id of the user
            val studentId = params[0]!!["studentId"] as String

            // Get user id in the database
            val userId = params[0]!!["userId"] as String

            // Storage reference for the cover photo
            val reference = storage.reference.child("cover/${studentId}.${getExtension(imageURI!!)}")

            // Start the upload task
            val uploadTask = reference.putFile(imageURI!!)

            // Handle the uploading process
            uploadTask.addOnProgressListener { taskSnapshot ->
                // The progress ratio
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount

                // If the progress is 100. It means that the uploading progress is done
                if (progress == 100.0) {
                    // Get download URL of the image that has just been uploaded and update it in the database
                    reference.downloadUrl.addOnSuccessListener { url ->
                        // Execute the AsyncTask to update avatar URL for the user in the database
                        UpdateUserInfoTask().execute(hashMapOf(
                            "userId" to userId,
                            "newCoverPhotoURL" to url.toString()
                        ))
                    }
                }
            }

            return null
        }
    }

    // AsyncTask for updating user info
    inner class UpdateUserInfoTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get user id in the database of the user
            val userId = params[0]!!["userId"] as String

            // Get url of the new cover photo
            val newCoverPhotoURL = params[0]!!["newCoverPhotoURL"] as String

            // Create the update user info service
            val updateUserInfoService: UpdateUserInfoService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
                UpdateUserInfoService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = updateUserInfoService.updateUserInfo(
                mapOfFields["avatarURL"] as String,
                newCoverPhotoURL,
                mapOfFields["phoneNumber"] as String,
                mapOfFields["facebook"] as String,
                mapOfFields["instagram"] as String,
                mapOfFields["twitter"] as String,
                mapOfFields["zalo"] as String,
                userId
            )

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is null, it means that info was not updated correctly
                    if (response.body() == null) {
                        // Show the user that the update was not successful
                        Toast.makeText(applicationContext, "Some thing is not right", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, "Updated", Toast.LENGTH_SHORT).show()

                        // Finish this activity
                        this@UpdateCoverPhoto.finish()
                    }
                }
            })

            return null
        }
    }

    // The function to get extension of the image
    private fun getExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }
}
