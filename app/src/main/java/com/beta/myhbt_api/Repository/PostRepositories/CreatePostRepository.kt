package com.beta.myhbt_api.Repository.PostRepositories

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.beta.myhbt_api.Controller.Posts.CreateNewPostPhotoService
import com.beta.myhbt_api.Controller.Posts.CreateNewPostService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor
import kotlin.math.floor

class CreatePostRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The user repository (to get info of the currently logged in user for some uses)
    private val userRepository = UserRepository(executor, context)

    // The photo repository
    private val photoRepository = PhotoRepository(executor, context)

    // The function to create new post in the database
    fun createNewPost (postContent: String, numOfImages: Int, selectedImages: ArrayList<Uri>) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the create post service
                val createPostService: CreateNewPostService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(CreateNewPostService::class.java)

                // The call object which will then be used to perform the API call
                val call: Call<Any> = createPostService.createNewHBTGramPost(postContent, userObject.getId(), numOfImages)

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
                            Toast.makeText(context, "Something is not right", Toast.LENGTH_SHORT).show()
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

                            //-------- Upload all photos to the storage
                            // Loop through all photos and them all to the database
                            for (image in selectedImages) {
                                // Call the function to upload new photo to the storage as well as its URL to the database
                                fileUploader(image) {imageURL ->
                                    // Call the function to post image URL of created post to the database
                                    postImageURLOfCreatedPost(imageURL, postId) {done, imageIDToLabel ->
                                        if (done) {
                                            // Call the function to label image
                                            photoRepository.labelImage(image, imageIDToLabel)
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    // The function to perform the file uploading procedure
    private fun fileUploader(imageURI: Uri, callback: (imageURL: String) -> Unit) {
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
                // Return uploaded image info via callback function
                callback(uri.toString())
            }
        }
    }

    // The function to post image URL of post that has just been created to the database
    fun postImageURLOfCreatedPost (imageURL: String, postId: String, callback: (done: Boolean, imageIDToLabel: String) -> Unit) {
        // Create the create post photo service
        val createPostPhotoService: CreateNewPostPhotoService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(CreateNewPostPhotoService::class.java)

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
                    Toast.makeText(context, "Something is not right", Toast.LENGTH_SHORT).show()
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

                            // Get image id of the image that has just been added to the database
                            val imageIDToLabel = (data["tour"] as Map<String, Any>)

                            // Return image id to label and image URL uploading status via callback function
                            callback(true, imageIDToLabel["_id"] as String)
                        } else {
                            print("Something is not right")

                            // Return error to via callback function
                            callback(false, "")
                        }

                        Toast.makeText(context, "Post created", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Something is not right", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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

    // The function to get extension of the image
    private fun getExtension(uri: Uri): String? {
        val contentResolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }
}