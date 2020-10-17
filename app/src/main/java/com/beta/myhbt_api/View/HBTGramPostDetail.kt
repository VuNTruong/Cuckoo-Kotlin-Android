package com.beta.myhbt_api.View

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.HBTGramPostComment
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPost
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPostDetail
import com.bumptech.glide.Glide
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
    private var arrayOfImages = ArrayList<String>()

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

        // Update adapter for the RecyclerView
        adapter = RecyclerViewAdapterHBTGramPostDetail(selectedPostObject, arrayOfImages, arrayOfComments, this)

        // Add adapter to the RecyclerView
        hbtGramPostDetailView.adapter = adapter

        // Call the function to set up post detail
        setUpPostDetail(selectedPostObject.getId())

        // Execute the AsyncTask to load avatar of the currently logged in user
        LoadAvatarOfCurrentUserTask().execute(hashMapOf(
            "userAvatarImageView" to userAvatarPostDetail
        ))

        // Set up event listener for the post comment button
        postCommentButtonPostDetail.setOnClickListener {
            // Execute the AsyncTask to create new comment for the post written by the currently logged in user
            GetCurrentUserInfoAndCreateComment().execute(hashMapOf(
                "postId" to selectedPostObject.getId(),
                "commentContentToPost" to commentContentToPostPostDetail.text.toString()
            ))
        }
    }

    // The function to set up post detail
    private fun setUpPostDetail (postId: String) {
        // Execute the AsyncTask to get all images of the post
        GetAllImagesTask().execute(hashMapOf(
            "postId" to postId
        ))

        // Execute the AsyncTask to get all comments fo the post
        GetAllCommentsTask().execute(hashMapOf(
            "postId" to postId
        ))
    }

    // AsyncTask for getting images of the post
    inner class GetAllImagesTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get id of the post
            val postId = params[0]!!["postId"] as String

            // Create the get first image URL service
            val getFirstImageURLService: GetFirstImageURLOfPostService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
                GetFirstImageURLOfPostService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getFirstImageURLService.getFirstPhotoURL(postId)

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

                        // Get array of images of the post
                        val arrayOfImagesOfPost = data["documents"] as List<Map<String, Any>>

                        // Loop through that array of image URLs from the database and add it to the
                        // array of images of the app
                        for (imageURL in arrayOfImagesOfPost) {
                            // Get URL of the image
                            val imageURL = imageURL["imageURL"] as String

                            // Add it to the array of image URLs
                            arrayOfImages.add(imageURL)
                        }

                        // Reload the RecyclerView
                        hbtGramPostDetailView.adapter!!.notifyDataSetChanged()
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask for loading all comments of the post
    inner class GetAllCommentsTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get id of the post
            val postId = params[0]!!["postId"] as String

            // Create the get post comments service
            val getPostCommentsService: GetHBTGramPostCommentsService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
                GetHBTGramPostCommentsService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getPostCommentsService.getPostComments(postId)

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

                        // Get array of comments of the post
                        val arrayOfCommentsOfPost = data["documents"] as List<Map<String, Any>>

                        // Loop through that array of comments from the database and add it to the
                        // array of comments of the app
                        for (comment in arrayOfCommentsOfPost) {
                            // Get content of the comment
                            val content = comment["content"] as String

                            // Get writer of the comment
                            val writer = comment["writer"] as String

                            // Create the object out of those info
                            val commentObject = HBTGramPostComment(content, writer)

                            // Add it to the array of comments
                            arrayOfComments.add(commentObject)
                        }

                        // Reload the RecyclerView
                        hbtGramPostDetailView.adapter!!.notifyDataSetChanged()
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to load avatar of the currently logged in user
    inner class LoadAvatarOfCurrentUserTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get the user avatar ImageView
            val userAvatarImageView = params[0]!!["userAvatarImageView"] as ImageView

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
                        val body = response.body()
                        print(body)
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get avatar of the user
                        val avatarURL = data["avatarURL"] as String

                        // Load that avatar URL into the ImageView
                        Glide.with(applicationContext)
                            .load(avatarURL)
                            .into(userAvatarImageView)
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to get email of the currently logged in user and create new comment base on it
    inner class GetCurrentUserInfoAndCreateComment : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get post id
            val postId = params[0]!!["postId"] as String

            // Get the content of the comment to post
            val commentContentToPost = params[0]!!["commentContentToPost"] as String

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

                        // Get email of the currently logged in user
                        val userEmail = data["email"] as String

                        // Execute the AsyncTask to create new comment for the post
                        CreateNewCommentTask().execute(hashMapOf(
                            "commentContentToPost" to commentContentToPost,
                            "commentWriterEmail" to userEmail,
                            "postId" to postId
                        ))
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to create new comment for the post
    inner class CreateNewCommentTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get the content of the comment to post
            val commentContentToPost = params[0]!!["commentContentToPost"] as String

            // Get email of the writer (currently logged in user)
            val commentWriterEmail = params[0]!!["commentWriterEmail"] as String

            // Get post id
            val postId = params[0]!!["postId"] as String

            // Create the create comment service
            val postCommentService: CreateNewHBTGramPostCommentService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
                CreateNewHBTGramPostCommentService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = postCommentService.createNewHBTGramPostComment(commentContentToPost, commentWriterEmail, postId)

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
                        // Create the object for the new comment
                        val commentObject = HBTGramPostComment(commentContentToPost, commentWriterEmail)

                        // Add it to the array of comments
                        arrayOfComments.add(commentObject)

                        // Reload the RecyclerView
                        hbtGramPostDetailView.adapter!!.notifyDataSetChanged()

                        // Empty content of the EditText
                        commentContentToPostPostDetail.setText("")
                    }
                }
            })

            return null
        }
    }
}
