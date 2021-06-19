package com.beta.cuckoo.View.UpdateUserInfo

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_update_cover_photo.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UpdateCoverPhoto : AppCompatActivity() {
    private val GALLERY_REQUEST_CODE = 1234
    private val TAG: String = "AppDebug"

    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // The user repository
    private lateinit var userRepository: UserRepository

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

        // Instantiate the user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Set on click listener for the back button
        backButtonUpdateCoverPhoto.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Call the function to get info of the currently logged in user as well as load current cover photo for the user
        getCurrentUserInfo()

        // Set on click listener for the choose new cover photo button
        chooseNewCoverImageButton.setOnClickListener {
            // Call the function top open the file chooser
            pickFromGallery()
        }

        // Set on click listener for the update cover photo button
        updateCoverPhotoButton.setOnClickListener {
            // Execute the AsyncTask to update the cover photo
            FileUploadingTask().execute()
        }
    }

    // The function to load chosen image into the image view
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }
                }
                else{
                    Log.e(TAG, "Image selection error: Couldn't select that image from memory." )
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    setImage(result.uri)
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e(TAG, "Crop error: ${result.error}" )
                }
            }
        }
    }

    // The function to get info of the currently logged in user
    fun getCurrentUserInfo () {
        // Call the function to get info of the currently logged in user
        userRepository.getInfoOfCurrentUser { userObject ->
            // Build the map of fields
            mapOfFields = hashMapOf(
                "fullName" to userObject.getFullName(),
                "email" to userObject.getEmail(),
                "userId" to userObject.getId(),
                "avatarURL" to userObject.getAvatarURL(),
                "coverURL" to userObject.getCoverURL(),
                "bio" to userObject.getBio()
            )

            // Load current avatar into the ImageView
            Glide.with(applicationContext)
                .load(userObject.getCoverURL())
                .into(userCoverPhotoUpdateCoverPhoto)
        }
    }

    // AsyncTask to perform file uploading procedure
    inner class FileUploadingTask: AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Call the function to get user id of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Storage reference for the cover photo
                val reference = storage.reference.child("cover/${userObject.getId()}.${getExtension(imageURI!!)}")

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
                            // Call the function to update user info
                            updateUserInfo(url.toString())
                        }
                    }
                }
            }

            return null
        }
    }

    // The function to update user info
    fun updateUserInfo (newCoverPhotoURL: String) {
        // Update cover photo URL
        mapOfFields["coverURL"] = newCoverPhotoURL

        // Call the function to update user info
        userRepository.updateCurrentUserInfo(mapOfFields) {done ->
            if (done) {
                // Finish this activity
                this@UpdateCoverPhoto.finish()
            }
        }
    }

    // The function to get extension of the image
    private fun getExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    // The function to set the selected image into preview image view
    private fun setImage(uri: Uri){
        imageURI = uri
        userCoverPhotoUpdateCoverPhoto.setImageURI(imageURI)
    }

    // The function to launch image crop
    private fun launchImageCrop(uri: Uri){
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(2000, 1000)
            .setCropShape(CropImageView.CropShape.RECTANGLE) // default is rectangle
            .start(this)
    }

    // The function to take user to the activity where user can pick an image
    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }
}
