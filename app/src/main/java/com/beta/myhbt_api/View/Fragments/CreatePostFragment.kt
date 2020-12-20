package com.beta.myhbt_api.View.Fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPostPhoto
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_create_post.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.floor

class CreatePostFragment : Fragment() {
    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterHBTGramPostPhoto?= null

    // Array of selected images for the post
    private val selectedImages = ArrayList<Uri>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the RecyclerView
        photoOfPostToCreate.layoutManager = LinearLayoutManager(this@CreatePostFragment.requireActivity())
        photoOfPostToCreate.itemAnimator = DefaultItemAnimator()

        // Update the adapter
        adapter = RecyclerViewAdapterHBTGramPostPhoto(selectedImages, this@CreatePostFragment.requireActivity(), this@CreatePostFragment)

        // Add adapter to the RecyclerView
        photoOfPostToCreate.adapter = adapter

        // Add on click listener for the choose photo button
        choosePhotoButton.setOnClickListener {
            // Call the function to open the file choose
            fileChooser()
        }

        // Set on click listener for the create post button
        createPostButton.setOnClickListener {
            // Call the function to get info of the current user and create new post based on it
            getUserInfoAndCreatePost(postContentToCreate.text.toString(), selectedImages.size)
        }
    }

    //******************************* CHOOSE IMAGE SEQUENCE *******************************
    /*
    In this sequence, we will do 2 things
    1. Let user choose image from file
    2. Load image into the list of chosen images
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
            selectedImages.add(data.data!!)
        }

        // Reload the RecyclerView
        photoOfPostToCreate.adapter!!.notifyDataSetChanged()
    }

    // We also have a function here which will help removing image from list of chosen images in the list
    // The function to update the RecyclerView after an image is removed from the list
    fun updateImageRecyclerView (position: Int) {
        // Remove the image at the specified position
        selectedImages.removeAt(position)

        // Update the RecyclerView
        photoOfPostToCreate.adapter!!.notifyDataSetChanged()
    }

    // The function to get extension of the image
    private fun getExtension(uri: Uri): String? {
        val contentResolver = this@CreatePostFragment.requireContext().contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }
    //******************************* END CHOOSE IMAGE SEQUENCE *******************************

    //******************************* CREATE POST SEQUENCE *******************************
    /*
    In this sequence, we will do 5 things
    1. Get info of the currently logged in user
    2. Create new post object in the post collection of the database
    3. Upload images of the post which is already in the array of chosen images
    4. Get download URL of uploaded images and add them to the image URL collection of the database
    5. Run the image labeler to get labels that is related to the image and add them to the image label collection of the database
     */

    // The function which will get user id of the currently logged in user and create new post based on it
    private fun getUserInfoAndCreatePost (postContent: String, numOfImages: Int) {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(this@CreatePostFragment.requireActivity())!!.create(
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

                    // Get user id of the current user
                    val userId = data["_id"] as String

                    // Call the function to create new post based on the obtained current user id
                    createNewPost(userId, postContent, numOfImages)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to add new post to the database
    fun createNewPost (writerUserId: String, postContent: String, numOfImages: Int) {
        // Create the create post service
        val createPostService: CreateNewHBTGramPostService = RetrofitClientInstance.getRetrofitInstance(this@CreatePostFragment.requireActivity())!!.create(
            CreateNewHBTGramPostService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = createPostService.createNewHBTGramPost(postContent, writerUserId, numOfImages)

        // Perform the API call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is null, it means that comment can't be posted
                if (response.body() == null) {
                    // Show the error
                    Toast.makeText(activity, "Something is not right", Toast.LENGTH_SHORT).show()
                } else {
                    // Get id of the post that has just been created
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get info of the post from the data
                    val postInfo = data["tour"] as Map<String, Any>

                    // Get id of the post that has just been created
                    val postId = postInfo["_id"] as String

                    // Loop through all photos and them all to the database
                    for (image in selectedImages) {
                        // Call the function to upload new photo to the storage as well as its URL to the database
                        fileUploader(image, postId)
                    }
                }
            }
        })
    }

    // The function to perform the file uploading procedure
    private fun fileUploader(imageURI: Uri, postId: String) {
        // Generate name for the image
        val imageName = generateRandomString(20)

        // Create the storage reference
        val storageReference =
            FirebaseStorage.getInstance().getReference("HBTGramPostPhotos")

        // Put name for the image
        val reference = storageReference.child("${imageName}.${getExtension(imageURI)}")

        // Start the upload task. This is the uploadTask which will be used to keep track of the upload process
        val uploadTask = reference.putFile(imageURI)

        // When uploading is done, get URL of that image
        uploadTask.addOnSuccessListener {
            // Get URL of the image that has just been uploaded to the storage
            reference.downloadUrl.addOnSuccessListener { uri ->
                // Call the function to create new image URL in the database for the post
                createNewImageURL(postId, uri.toString(), imageURI)
            }
        }
    }

    // AsyncTask for adding new image URL to the database
    // The function for adding new image URL to the database
    private fun createNewImageURL (postId: String, imageURL: String, imageURI: Uri) {
        // Create the create post photo service
        val createPostPhotoService: CreateNewHBTGramPostPhotoService =
            RetrofitClientInstance.getRetrofitInstance(this@CreatePostFragment.requireActivity())!!
                .create(
                    CreateNewHBTGramPostPhotoService::class.java
                )

        // The call object which will then be used to perform the API call
        val call: Call<Any> = createPostPhotoService.createNewHBTGramPostPhoto(postId, imageURL)

        // Perform the API call
        call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is null, it means that comment can't be posted
                if (response.body() == null) {
                    // Show the error
                    Toast.makeText(activity, "Something is not right", Toast.LENGTH_SHORT).show()
                } else {
                    // Get status code of the response
                    val statusCode = response.code()

                    // If the status code is 201, show the toast and let user know that post has been created
                    if (statusCode == 201) {
                        // If the response body is not empty it means that image URL is uploaded
                        if (response.body() != null) {
                            val body = response.body()
                            print(body)
                            // Body of the request
                            val responseBody = response.body() as Map<String, Any>

                            // Get data from the response body
                            val data = responseBody["data"] as Map<String, Any>

                            // Get new image URL object info
                            val imageURLInfo = data["tour"] as Map<String, Any>

                            // Execute the AsyncTask to label image
                            LabelImagesTask().execute(hashMapOf(
                                "imageToLabel" to imageURI,
                                "imageIDToLabel" to imageURLInfo["_id"] as String
                            ))
                        } else {
                            print("Something is not right")
                        }

                        Toast.makeText(activity, "Post created", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "Something is not right", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    //******************************* END CREATE POST SEQUENCE *******************************

    //******************************* LABEL IMAGES SEQUENCE *******************************
    // AsyncTask to label images of the post
    private inner class LabelImagesTask: AsyncTask<HashMap<String, Any>, Void, Void> () {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get image to label
            val imageToLabel = params[0]!!["imageToLabel"] as Uri

            // Get image ID of the image to label
            val imageIDToLabel = params[0]!!["imageIDToLabel"] as String

            // Image labeler (powered by FirebaseVision)
            val labeler = FirebaseVision.getInstance().visionCloudLabelDetector

            // The function to convert image uri into bitmap
            fun convertToBitmap (imageURI: Uri) : Bitmap {
                return MediaStore.Images.Media.getBitmap(this@CreatePostFragment.requireActivity().contentResolver, imageURI)
            }

            // Convert the image to bitmap
            val bitmap = convertToBitmap(imageToLabel)

            // Create the firebase vision image object which can be used to label by the labeler
            val image = FirebaseVisionImage.fromBitmap(bitmap)

            // Start labeling the image
            labeler.detectInImage(image)
                .addOnSuccessListener {labels ->
                    // Loop through list of labels and add them to the database
                    for (label in labels) {
                        // Get info of the labeled image
                        val text = label.label

                        // Call the function to upload info of the labeled image to the database
                        uploadLabeledImageInfoToDatabase(imageIDToLabel, text)
                    }
                }.addOnFailureListener {
                    print("Task failed")
                }

            return null
        }

        // The function to upload labeled image info to the database
        fun uploadLabeledImageInfoToDatabase (imageID: String, imageLabel: String) {
            // Create the create hbt gram post photo label service
            val createHBTGramPostPhotoLabelService: CreateNewHBTGramPostPhotoLabelService = RetrofitClientInstance.getRetrofitInstance(this@CreatePostFragment.requireActivity())!!.create(
                CreateNewHBTGramPostPhotoLabelService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = createHBTGramPostPhotoLabelService.createNewHBTGramPostPhotoLabel(imageID, imageLabel)

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is null, it means that comment can't be posted
                    if (response.body() == null) {
                        // Show the error
                        Toast.makeText(activity, "Something is not right", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
    //******************************* END LABEL IMAGES SEQUENCE *******************************

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