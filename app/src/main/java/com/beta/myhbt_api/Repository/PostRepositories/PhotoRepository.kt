package com.beta.myhbt_api.Repository.PostRepositories

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.widget.Toast
import com.beta.myhbt_api.Controller.Posts.CreateNewPostPhotoLabelService
import com.beta.myhbt_api.Controller.Posts.GetPhotoLabelBasedOnImageIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Controller.UserStats.UpdateUserPhotoLabelVisitService
import com.beta.myhbt_api.Model.HBTGramPostPhotoLabel
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URI
import java.util.concurrent.Executor

class PhotoRepository (executor: Executor, context: Context) {
    // The user repository (to get info of the currently logged in user for some uses)
    private val userRepository = UserRepository(executor, context)

    // In order to prevent us from encountering the class cast exception, we need to do the following
    // Create the GSON object
    private val gs = Gson()

    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The function to get photo labels of a photo based on photo id
    fun getPhotoLabelsBasedOnId (photoId: String, callback: (arrayOfPhotoLabels: ArrayList<HBTGramPostPhotoLabel>) -> Unit) {
        executor.execute {
            // Create the get photo labels service
            val getPhotoLabelBasedOnImageIdService: GetPhotoLabelBasedOnImageIdService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetPhotoLabelBasedOnImageIdService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getPhotoLabelBasedOnImageIdService.getPhotoLabels(photoId)

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

                        // Get data of the response
                        val data = responseBody["data"] as Map<String, Any>

                        // Get list of photo labels from the data
                        val imageLabelsLinkedTreeMap = data["documents"] as List<Map<String, Any>>

                        // Array of image labels that has already been converted from linked tree map to Kotlin objects
                        val arrayOfImageLabels = ArrayList<HBTGramPostPhotoLabel>()

                        // Loop through list of photo labels to update photo label visit status for the current user
                        for (imageLabel in imageLabelsLinkedTreeMap) {
                            // Convert the image label object which is currently a linked tree map into a JSON string
                            val jsPhotoLabel = gs.toJson(data)

                            // Convert the JSON string back into User class
                            val photoLabelObject = gs.fromJson<HBTGramPostPhotoLabel>(jsPhotoLabel, HBTGramPostPhotoLabel::class.java)

                            arrayOfImageLabels.add(photoLabelObject)
                        }

                        // Return array of photo label to the view controller via callback function
                        callback(arrayOfImageLabels)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to update photo label visit of the currently logged in user
    fun updatePhotoLabelVisitForCurrentUser (photoLabel: HBTGramPostPhotoLabel, callback: (updated: Boolean) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the update photo label visit service
                val updateUserPhotoLabelVisitService: UpdateUserPhotoLabelVisitService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    UpdateUserPhotoLabelVisitService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = updateUserPhotoLabelVisitService.updatePhotoLabelVisit(userObject.getId(), photoLabel.getImageLabel())

                // Perform the call
                call.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        // Let the view model know that photo label has been unsuccessfully updated via callback function
                        callback(false)
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // Let the view model know that photo label has bee successfully updated via callback function
                        callback(true)
                    }
                })
            }
        }
    }

    // The function to label image
    fun labelImage (imageURI: Uri, imageID: String) {
        executor.execute {
            // Image labeler (powered by FirebaseVision)
            val labeler = FirebaseVision.getInstance().visionCloudLabelDetector

            // The function to convert image uri into bitmap
            fun convertToBitmap (imageURI: Uri) : Bitmap {
                return MediaStore.Images.Media.getBitmap(context.contentResolver, imageURI)
            }

            // Convert the image to bitmap
            val bitmap = convertToBitmap(imageURI)

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
                        uploadLabeledImageInfoToDatabase(imageID, text)
                    }
                }.addOnFailureListener {
                    print("Task failed")
                }
        }
    }

    // The function to upload labeled image info to the database
    private fun uploadLabeledImageInfoToDatabase (imageID: String, imageLabel: String) {
        // Create the create hbt gram post photo label service
        val createHBTGramPostPhotoLabelService: CreateNewPostPhotoLabelService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
            CreateNewPostPhotoLabelService::class.java)

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
                    Toast.makeText(context, "Something is not right", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}