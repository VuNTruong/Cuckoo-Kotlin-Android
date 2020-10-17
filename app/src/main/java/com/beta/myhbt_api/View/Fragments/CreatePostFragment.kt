package com.beta.myhbt_api.View.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.CreateNewHBTGramPostPhotoService
import com.beta.myhbt_api.Controller.CreateNewHBTGramPostService
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPostPhoto
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
            // Execute the AsyncTask to create new post
            GetCurrentUserInfoAndCreatePost().execute(hashMapOf(
                "content" to postContentToCreate.text.toString(),
                "numOfImages" to selectedImages.size
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

    // AsyncTask which will get email of the currently logged in user and create new post based on it
    inner class GetCurrentUserInfoAndCreatePost : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get content of the post
            val content = params[0]!!["content"] as String

            // Get number of images of the post
            val numOfImages = params[0]!!["numOfImages"] as Int

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

                        // Get email of the currently logged in user
                        val userEmail = data["email"] as String

                        // Execute the ASyncTask to add new like for the post
                        CreateNewPostTask().execute(hashMapOf(
                            "writerEmail" to userEmail,
                            "content" to content,
                            "numOfImages" to numOfImages
                        ))
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask for adding new post
    inner class CreateNewPostTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get email of the post writer (currently logged in user)
            val writer = params[0]!!["writerEmail"] as String

            // Get the post content
            val content = params[0]!!["content"] as String

            // Get number of images
            val numOfImages = params[0]!!["numOfImages"] as Int

            // Create the create post service
            val createPostService: CreateNewHBTGramPostService = RetrofitClientInstance.getRetrofitInstance(this@CreatePostFragment.requireActivity())!!.create(
                CreateNewHBTGramPostService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = createPostService.createNewHBTGramPost(content, writer, numOfImages)

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

            return null
        }
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
                // Execute the AsyncTask to upload new image URL to the database
                CreateNewImageURLTask().execute(hashMapOf(
                    "postId" to postId,
                    "imageURL" to uri.toString()
                ))
            }
        }
    }

    // AsyncTask for adding new image URL to the database
    inner class CreateNewImageURLTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get post id
            val postId = params[0]!!["postId"] as String

            // Get image URL
            val imageURL = params[0]!!["imageURL"] as String

            // Create the create post photo service
            val createPostPhotoService: CreateNewHBTGramPostPhotoService = RetrofitClientInstance.getRetrofitInstance(this@CreatePostFragment.requireActivity())!!.create(
                CreateNewHBTGramPostPhotoService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = createPostPhotoService.createNewHBTGramPostPhoto(postId, imageURL)

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {

                }
            })

            return null
        }
    }

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