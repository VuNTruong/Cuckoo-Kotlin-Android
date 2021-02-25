package com.beta.myhbt_api.Repository.PostRepositories

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.beta.myhbt_api.Model.PostPhoto
import com.beta.myhbt_api.Network.RetrofitClientInstance
import com.beta.myhbt_api.Network.UserStats.UpdateUserPhotoLabelVisitService
import com.beta.myhbt_api.Model.PostPhotoLabel
import com.beta.myhbt_api.Network.Posts.*
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterProfileDetail
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterRecommendAlbum
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_profile_detail.*
import kotlinx.android.synthetic.main.fragment_recommend_photo.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
    fun getPhotoLabelsBasedOnId (photoId: String, callback: (arrayOfPhotoLabels: ArrayList<PostPhotoLabel>) -> Unit) {
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
                        val arrayOfImageLabels = ArrayList<PostPhotoLabel>()

                        // Loop through list of photo labels to update photo label visit status for the current user
                        for (imageLabel in imageLabelsLinkedTreeMap) {
                            // Convert the image label object which is currently a linked tree map into a JSON string
                            val jsPhotoLabel = gs.toJson(data)

                            // Convert the JSON string back into User class
                            val photoLabelObject = gs.fromJson<PostPhotoLabel>(jsPhotoLabel, PostPhotoLabel::class.java)

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
    fun updatePhotoLabelVisitForCurrentUser (photoLabel: PostPhotoLabel, callback: (updated: Boolean) -> Unit) {
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

    //---------------------------------- LABEL IMAGES ----------------------------------
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
        executor.execute {
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
    //---------------------------------- END LABEL IMAGES ----------------------------------

    //---------------------------------- GET RECOMMENDED PHOTOS ----------------------------------
    // The function to get list of recommended photos for the currently logged in user
    fun getRecommendedPhotos (currentLocationInList: Int, callback: (arrayOfRecommendedPhotos: ArrayList<PostPhoto>, newCurrentLocationInList: Int) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the get recommended list of photos service
                val getRecommendedPhotosForUserService: GetRecommendedPhotosForUserService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetRecommendedPhotosForUserService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = getRecommendedPhotosForUserService.getPostPhotosForUser(userObject.getId(), currentLocationInList)

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

                            // Get data from the response body (list of photos recommended for the user)
                            val arrayOfRecommendedPhotos = responseBody["data"] as ArrayList<PostPhoto>

                            // Get new current location in list for the user so that the app know where to go next
                            val newCurrentLocationInList = (responseBody["newCurrentLocationInList"] as Double).toInt()

                            // Return array of recommended photos and new current location in list via callback function
                            callback(arrayOfRecommendedPhotos, newCurrentLocationInList)
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to get order in collection of latest photo in the database
    fun getOrderInCollectionOfLatestPhoto (callback: (orderInCollectionOfLatestPhoto: Int) -> Unit) {
        executor.execute {
            // Create the get order in collection of latest photo label service
            val getOrderInCollectionOfLatestPostPhotoLabelService: GetOrderInCollectionOfLatestPostPhotoLabelService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetOrderInCollectionOfLatestPostPhotoLabelService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getOrderInCollectionOfLatestPostPhotoLabelService.getOrderInCollectionOfLatestPostPhoto()

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

                        // Get data from the response body (order in collection of latest photo label in collection)
                        val orderInCollectionOfLatestPhoto = (responseBody["data"] as Double).toInt()

                        // Return order in collection of latest photo via callback function
                        callback(orderInCollectionOfLatestPhoto)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }
    //---------------------------------- END GET RECOMMENDED PHOTOS ----------------------------------

    // The function to get photos created by user with specified user id
    fun getPhotosOfUserWithId (userId: String, callback: (arrayOfImagesByUser: ArrayList<PostPhoto>) -> Unit) {
        executor.execute {
            // Create the get images of user service
            val getPhotosOfUserService: GetPhotosOfUserService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetPhotosOfUserService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getPhotosOfUserService.getPhotosOfUser(userId)

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

                        // Get data from the response body (array of images created by the user)
                        val arrayOfImagesByUser = responseBody["data"] as ArrayList<PostPhoto>

                        // Return array of image by user via callback function
                        callback(arrayOfImagesByUser)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }
}