package com.beta.myhbt_api.View

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.BackgroundServices
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Interfaces.CreateNotificationInterface
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.HBTGramPostComment
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.Model.HBTGramPostPhotoLabel
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPostDetail
import com.google.gson.Gson
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_hbtgram_post_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HBTGramPostDetail : AppCompatActivity(), CreateNotificationInterface {
    // User id of the current user
    private var currentUserId = ""

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterHBTGramPostDetail?= null

    // Selected post object
    private var selectedPostObject = HBTGramPost("", "", "", 0, 0, "")

    // Array of image URL of the post
    private var arrayOfImages = ArrayList<HBTGramPostPhoto>()

    // Array of comments of the post
    private var arrayOfComments = ArrayList<HBTGramPostComment>()

    // In order to prevent us from encountering the class cast exception, we need to do the following
    // Create the GSON object
    private val gs = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hbtgram_post_detail)

        // Hide the action bar
        supportActionBar!!.hide()

        // Get the selected post object from previous activity
        selectedPostObject = intent.getSerializableExtra("selectedPostObject") as HBTGramPost

        // Instantiate the recycler view
        hbtGramPostDetailView.layoutManager = LinearLayoutManager(applicationContext)
        hbtGramPostDetailView.itemAnimator = DefaultItemAnimator()

        // Set up on click listener for the post comment button
        postCommentButtonPostDetail.setOnClickListener {
            // Call the function to create new comment sent to the post by the current user
            createNewComment(commentContentToPostPostDetail, selectedPostObject.getId())
        }

        // Set up on click listener for the send image as comment button
        selectPictureForCommentButton.setOnClickListener {
            // Start the activity where the user can choose image to send
            // The intent object
            val intent = Intent(applicationContext, HBTGramPostDetailCommentSendImage::class.java)

            // Put post id into the intent so that next activity will know which post to work with
            intent.putExtra("postId", selectedPostObject.getId())

            // Start the activity
            startActivity(intent)
        }

        // Call the function to get post detail
        getPostDetail(selectedPostObject.getId())

        // Call the function to get info of the current user
        getCurrentUserInfo()

        // Call the function to set up socket.io and make everything real time
        setUpSocketIO()
    }


    // THe function to set up socket.IO
    private fun setUpSocketIO () {
        //************************ DO THINGS WITH THE SOCKET.IO ************************
        // Bring user into the post detail room
        BackgroundServices.mSocket.emit("jumpInPostDetailRoom", gs.toJson(hashMapOf(
            "postId" to selectedPostObject.getId()
        )))

        // Listen to event of when new comment is added to the post
        BackgroundServices.mSocket.on("updateComment", onUpdateComment)

        // Listen to event of when new comment with photo is added to the post
        BackgroundServices.mSocket.on("updateCommentWithPhoto", onUpdateCommentWithPhoto)
        //************************ END WORKING WITH SOCKET.IO ************************v
    }

    //************************* CALL BACK FUNCTIONS FOR SOCKET.IO *************************
    // The callback function to update comment when the new one is added to the post
    private var onUpdateComment = Emitter.Listener {
        // New comment object from the server
        val commentObject: HBTGramPostComment = gs.fromJson(it[0].toString(), HBTGramPostComment::class.java)

        // Since this will update the view, it MUST run on the UI thread
        runOnUiThread{
            // Add new comment to the array of comments
            arrayOfComments.add(commentObject)

            // Update the RecyclerView
            hbtGramPostDetailView.adapter!!.notifyDataSetChanged()
        }
    }

    // The callback function to update comment when new one with photo is added to the post
    private var onUpdateCommentWithPhoto = Emitter.Listener {
        // New comment object from the server
        val commentObject: HBTGramPostComment = gs.fromJson(it[0].toString(), HBTGramPostComment::class.java)

        // Since this will update the view, it MUST run on the UI thread
        runOnUiThread{
            // Add new comment to the array of comments
            arrayOfComments.add(commentObject)

            // Update the RecyclerView
            hbtGramPostDetailView.adapter!!.notifyDataSetChanged()
        }
    }
    //************************* END CALL BACK FUNCTIONS FOR SOCKET.IO *************************

    //*********************************** CREATE NEW COMMENT SEQUENCE ***********************************
    // The function to get user id of the current user
    private fun getCurrentUserInfo () {
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
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user id in the database of the currently logged in user
                    val userId = data["_id"] as String

                    // Update the current user id variable of this activity
                    currentUserId = userId
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to create new comment for the post
    private fun createNewComment (commentContentToPostEditText: EditText, postId: String) {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // Create the create comment service
        val postCommentService: CreateNewHBTGramPostCommentService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            CreateNewHBTGramPostCommentService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = postCommentService.createNewHBTGramPostComment(commentContentToPostEditText.text.toString(), currentUserId, postId)

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

                    // Emit event to the server and let the server know that new comment has been added
                    BackgroundServices.mSocket.emit("newComment", gs.toJson(hashMapOf(
                        "commentId" to newCommentId,
                        "writer" to currentUserId,
                        "content" to commentContentToPostEditText.text.toString(),
                        "postId" to selectedPostObject.getId()
                    )))

                    // Convert photo object which is currently a linked tree map into a JSON string
                    val jsPhoto = gs.toJson(arrayOfImages[0])

                    // Convert the JSOn string back into PostPhoto class
                    val photoObject = gs.fromJson<HBTGramPostPhoto>(jsPhoto, HBTGramPostPhoto::class.java)

                    // Call the function to create new notification for the post writer
                    createNotification("commented", selectedPostObject.getWriter(), currentUserId, photoObject.getImageURL(), selectedPostObject.getId())

                    // Create new comment object based on info of the newly created comment
                    val newCommentObject = HBTGramPostComment(commentContentToPostEditText.text.toString(), currentUserId, newCommentId)

                    // Add the newly created comment object to the array
                    arrayOfComments.add(newCommentObject)

                    // Empty content of the EditText
                    commentContentToPostEditText.setText("")

                    // Reload the RecyclerView
                    hbtGramPostDetailView.adapter!!.notifyDataSetChanged()
                }
            }
        })
    }
    //*********************************** END CREATE NEW COMMENT SEQUENCE ***********************************

    //*********************************** GET POST DETAIL SEQUENCE ***********************************
    // The function to load post info of the selected post
    private fun getPostDetail (postId: String) {
        // Create the get post detail service
        val getHBTGramPostDetail: GetHBTGramPostDetail = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetHBTGramPostDetail::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getHBTGramPostDetail.getPostDetail(postId)

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

                    // Get array of images of the post
                    arrayOfImages = responseBody["arrayOfImages"] as ArrayList<HBTGramPostPhoto>

                    // Get number of likes of the post
                    val numOfLikes = (responseBody["numOfLikes"] as Double).toInt()

                    // Get number of comments of the post
                    val numOfComments = (responseBody["numOfComments"] as Double).toInt()

                    // Get array of comments
                    arrayOfComments = responseBody["arrayOfComments"] as ArrayList<HBTGramPostComment>

                    // Update adapter for the RecyclerView
                    adapter = RecyclerViewAdapterHBTGramPostDetail(selectedPostObject, arrayOfImages, numOfComments, numOfLikes, arrayOfComments, this@HBTGramPostDetail)

                    // Add adapter to the RecyclerView
                    hbtGramPostDetailView.adapter = adapter

                    // Loop through list of photos of the post to update user photo label visit status
                    for (i in 0 until arrayOfImages.size) {
                        // Convert the image object which is currently a linked tree map into a JSON string
                        val jsPhoto = gs.toJson(arrayOfImages[i])

                        // Convert the JSON string back into User class
                        val photoObject = gs.fromJson<HBTGramPostPhoto>(jsPhoto, HBTGramPostPhoto::class.java)

                        // Call the function to update user photo label visit status for the current user
                        getPhotoLabelsBasedOnIdAndUpdatePhotoLabelVisitStatus(photoObject.getPostId())
                    }
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //*********************************** END GET POST DETAIL SEQUENCE ***********************************

    //*********************************** GET AND UPDATE PHOTO LABEL VISIT ***********************************
    // The function to get photo labels associated with photos of the post based on their photo ids
    fun getPhotoLabelsBasedOnIdAndUpdatePhotoLabelVisitStatus (photoId: String) {
        // Create the get photo labels service
        val getPhotoLabelBasedOnImageIdService: GetPhotoLabelBasedOnImageIdService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
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
                    val imageLabels = data["documents"] as List<Map<String, Any>>

                    // Loop through list of photo labels to update photo label visit status for the current user
                    for (imageLabel in imageLabels) {
                        // Convert the image label object which is currently a linked tree map into a JSON string
                        val jsPhotoLabel = gs.toJson(data)

                        // Convert the JSON string back into User class
                        val photoLabelObject = gs.fromJson<HBTGramPostPhotoLabel>(jsPhotoLabel, HBTGramPostPhotoLabel::class.java)

                        // Call the function to update user photo label visit status
                        updateUserPhotoLabelVisitStatus(photoLabelObject.getImageLabel())
                    }
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to update photo label visit status for the current user
    fun updateUserPhotoLabelVisitStatus (imageLabel: String) {
        // Create the update photo label visit service
        val updateUserPhotoLabelVisitService: UpdateUserPhotoLabelVisitService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            UpdateUserPhotoLabelVisitService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = updateUserPhotoLabelVisitService.updatePhotoLabelVisit(currentUserId, imageLabel)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                print("Done")
            }
        })
    }
    //*********************************** END GET AND UPDATE PHOTO LABEL VISIT ***********************************

    //******************************** CREATE NOTIFICATION SEQUENCE ********************************
    // The function to create new notification
    override fun createNotification (content: String, forUser: String, fromUser: String, image: String, postId: String) {
        // Create the create notification service
        val createNotificationService: CreateNotificationService = RetrofitClientInstance.getRetrofitInstance(this)!!.create(
            CreateNotificationService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = createNotificationService.createNewNotification(content, forUser, fromUser, image, postId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {

            }
        })
    }
    //******************************** END CREATE NOTIFICATION SEQUENCE ********************************
}
