package com.beta.myhbt_api.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.HBTGramPostComment
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPostDetail
import kotlinx.android.synthetic.main.activity_hbtgram_post_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HBTGramPostDetail : AppCompatActivity() {
    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterHBTGramPostDetail?= null

    // Selected post object
    private var selectedPostObject = HBTGramPost("", "", "", 0, 0, "")

    // Array of image URL of the post
    private var arrayOfImages = ArrayList<HBTGramPostPhoto>()

    // Array of comments of the post
    private var arrayOfComments = ArrayList<HBTGramPostComment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hbtgram_post_detail)

        // Get the selected post object from previous activity
        selectedPostObject = intent.getSerializableExtra("selectedPostObject") as HBTGramPost

        // Instantiate the recycler view
        hbtGramPostDetailView.layoutManager = LinearLayoutManager(applicationContext)
        hbtGramPostDetailView.itemAnimator = DefaultItemAnimator()

        // Set up on click listener for the post comment button
        postCommentButtonPostDetail.setOnClickListener {
            // Call the function to get info of the current user and create new comment based on it
            getUserInfoAndCreateComment(commentContentToPostPostDetail, selectedPostObject.getId())
        }

        // Call the function to get post detail
        getPostDetail(selectedPostObject.getId())
    }

    //*********************************** CREATE NEW COMMENT SEQUENCE ***********************************
    // The function to get user id of the current user and create comment based on that
    private fun getUserInfoAndCreateComment (commentToPostContentEditText: EditText, postId: String) {
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

                    // Get user id in the database of the currently logged in user
                    val userId = data["_id"] as String

                    // Call the function to create new comment sent to the post by the current user
                    createNewComment(commentToPostContentEditText, userId, postId)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to create new comment for the post
    fun createNewComment (commentContentToPostEditText: EditText, commentWriterUserId: String, postId: String) {
        // Create the create comment service
        val postCommentService: CreateNewHBTGramPostCommentService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            CreateNewHBTGramPostCommentService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = postCommentService.createNewHBTGramPostComment(commentContentToPostEditText.text.toString(), commentWriterUserId, postId)

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
                    // Create new comment object based on info of the newly created comment
                    val newCommentObject = HBTGramPostComment(commentContentToPostEditText.text.toString(), commentWriterUserId)

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
                } else {
                    print("Something is not right")
                }
            }
        })
    }
}
