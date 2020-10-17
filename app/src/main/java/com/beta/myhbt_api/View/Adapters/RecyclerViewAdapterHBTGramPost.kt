package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.HBTGramPostComment
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Fragments.DashboardFragment
import com.beta.myhbt_api.View.HBTGram
import com.beta.myhbt_api.View.HBTGramPostDetail
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterHBTGramPost (hbtGramPostObjects: ArrayList<HBTGramPost>, activity: Activity, hbtGram: DashboardFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of HBTGram posts
    private val hbtGramPostObjects = hbtGramPostObjects

    // Activity of the parent activity
    private val activity = activity

    // The parent activity
    private val hbtGram = hbtGram

    // ViewHolder for the post item
    inner class ViewHolderHBTGramPost internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val postWriterAvatar: ImageView = itemView.findViewById(R.id.writerAvatar)
        private val writerFullName: TextView = itemView.findViewById(R.id.writerFullName)
        private val dateCreated: TextView = itemView.findViewById(R.id.dateCreated)
        private val postContent: TextView = itemView.findViewById(R.id.postContent)
        private val postPhoto: ImageView = itemView.findViewById(R.id.postPhoto)
        private val numOfLikes: TextView = itemView.findViewById(R.id.numOfLikes)
        private val numOfComments: TextView = itemView.findViewById(R.id.numOfComments)
        private val likeButton: ImageView = itemView.findViewById(R.id.likeButton)
        private val commentButton: ImageView = itemView.findViewById(R.id.commentButton)
        private val userAvatar: ImageView = itemView.findViewById(R.id.userAvatar)
        private val commentToPostContent: EditText = itemView.findViewById(R.id.commentToPostContent)
        private var postCommentButton: ImageView = itemView.findViewById(R.id.postCommentButton)

        // The function to set up post info
        fun setUpPostInfo (postObject: HBTGramPost) {
            // Execute the AsyncTask to load avatar and full name of the post writer
            GetUserInfoTask().execute(hashMapOf(
                "writerEmail" to postObject.getWriter(),
                "fullNameTextView" to writerFullName,
                "avatarImageView" to postWriterAvatar
            ))

            // Execute the AsyncTask to load first image of the post into the ImageView
            GetFirstPhotoTask().execute(hashMapOf(
                "postId" to postObject.getId(),
                "postPhotoImageView" to postPhoto
            ))

            // Execute the AsyncTask to load avatar of the currently logged in user
            LoadAvatarOfCurrentUserTask().execute(hashMapOf(
                "userAvatarImageView" to userAvatar
            ))

            // Execute the AsyncTask to load number of comments
            GetNumberOfCommentsTask().execute(
                hashMapOf(
                    "numOfCommentsTextView" to numOfComments,
                    "postId" to postObject.getId()
                )
            )

            // Execute the AsyncTask to load number of likes
            GetNumberOfLikesTask().execute(
                hashMapOf(
                    "numOfLikesTextView" to numOfLikes,
                    "postId" to postObject.getId()
                )
            )

            // Set on click listener for the comment button so that it will take user to the activity where the
            // user can see post detail
            commentButton.setOnClickListener {
                // Call the function
                gotoPostDetail(postObject)
            }

            // Set on click listener for the like button
            likeButton.setOnClickListener {
                // Execute the AsyncTask and add like to the post
                GetCurrentUserInfoAndCreateLike().execute(hashMapOf(
                    "postId" to postObject.getId()
                ))
            }

            // Set on click listener for the post comment button
            postCommentButton.setOnClickListener {
                // Execute the AsyncTask to create new comment for the post
                GetCurrentUserInfoAndCreateComment().execute(hashMapOf(
                    "postId" to postObject.getId(),
                    "commentContentToPost" to commentToPostContent.text.toString(),
                    "commentToPostEditText" to commentToPostContent
                ))
            }

            // Load other info
            dateCreated.text = postObject.getDateCreated()
            postContent.text = postObject.getContent()
        }
    }

    // AsyncTask to get info of the post writer
    private inner class GetUserInfoTask: AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get email of the post writer
            val writerEmail = params[0]!!["writerEmail"] as String

            // Get the full name TextView
            val fullNameTextView = params[0]!!["fullNameTextView"] as TextView

            // Get the post writer avatar ImageView
            val avatarImageView = params[0]!!["avatarImageView"] as ImageView

            // Create the get user info service
            val getUserInfoService: GetUserInfoService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(GetUserInfoService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getUserInfoService.getUserInfo(writerEmail)

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

                        // Get user info from the received data
                        val userInfo = (data["documents"] as List<Map<String, Any>>)[0]

                        // Combine them all to get the full name
                        val fullName = "${userInfo["firstName"] as String} ${userInfo["middleName"] as String} ${userInfo["lastName"] as String}"

                        // Get avatar of the user
                        val userAvatar = userInfo["avatarURL"] as String

                        // Load full name into the TextView
                        fullNameTextView.text = fullName

                        // Load avatar info the ImageView
                        Glide.with(activity)
                            .load(userAvatar)
                            .into(avatarImageView)
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to get URL of first photo of the post
    inner class GetFirstPhotoTask: AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get id of the post
            val postId = params[0]!!["postId"] as String

            // Get the post photo ImageView
            val postPhotoImageView = params[0]!!["postPhotoImageView"] as ImageView

            // Create the get first image URL service
            val getFirstImageURLService: GetFirstImageURLOfPostService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(GetFirstImageURLOfPostService::class.java)

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

                        // Get the array of images
                        val arrayOfImages = data["documents"] as List<Map<String, Any>>

                        if (arrayOfImages.isNotEmpty()) {
                            // Get image info from the received data
                            val firstImageInfo = (data["documents"] as List<Map<String, Any>>)[0]

                            // Get URL of the image
                            val firstImageURL = firstImageInfo["imageURL"] as String

                            // Load that URL into the ImageView
                            Glide.with(activity)
                                .load(firstImageURL)
                                .into(postPhotoImageView)
                        }
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to load avatar of the currently logged in user
    inner class LoadAvatarOfCurrentUserTask: AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get user avatar image view
            val userAvatarImageView = params[0]!!["userAvatarImageView"] as ImageView

            // Create the validate token service
            val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
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

                        // Get avatar URL of the user
                        val avatarURL = data["avatarURL"] as String

                        // Load that avatar URL into the ImageView
                        Glide.with(activity)
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

    // AsyncTask to get number of comments of the post
    inner class GetNumberOfCommentsTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get the number of comments TextView
            val numOfCommentsTextView = params[0]!!["numOfCommentsTextView"] as TextView

            // Get id of the post
            val postId = params[0]!!["postId"] as String

            // Create the get post comments service
            val getPostCommentsService: GetHBTGramPostCommentsService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
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

                        // Get number of comments
                        val numOfComments = (responseBody["results"] as Double).toInt()

                        // Load the number of comments into the TextView
                        numOfCommentsTextView.text = "$numOfComments comments"
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask for getting number of likes of the post
    inner class GetNumberOfLikesTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get the number of likes TextView
            val numOfLikesTextView = params[0]!!["numOfLikesTextView"] as TextView

            // Get id of the post
            val postId = params[0]!!["postId"] as String

            // Create the get post likes service
            val getPostLikesService: GetAllHBTGramPostLikesService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
                GetAllHBTGramPostLikesService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getPostLikesService.getPostLikes(postId)

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

                        // Get number of comments
                        val numOfComments = (responseBody["results"] as Double).toInt()

                        // Load the number of likes into the TextView
                        numOfLikesTextView.text = "$numOfComments likes"
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask which will get email of the currently logged in user and create new like based on it
    inner class GetCurrentUserInfoAndCreateLike : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get the post id
            val postId = params[0]!!["postId"] as String

            // Create the get current user info service
            val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
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
                        CreateNewLikeTask().execute(hashMapOf(
                            "likerEmail" to userEmail,
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

    // AsyncTask for adding new like for the post
    inner class CreateNewLikeTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get email of the liker (currently logged in user)
            val likerEmail = params[0]!!["likerEmail"] as String

            // Get post id
            val postId = params[0]!!["postId"] as String

            // Create the add like service
            val addLikeService: CreateNewHBTGramPostLikeService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
                CreateNewHBTGramPostLikeService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = addLikeService.createNewHBTGramPostLike(likerEmail, postId)

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
                        // Done
                        Toast.makeText(activity, "You've liked this post", Toast.LENGTH_SHORT).show()

                        // Reload the RecyclerView
                        hbtGram.reloadRecyclerView()
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

            // Get the comment content to post edit text
            val commentToPostEditText = params[0]!!["commentToPostEditText"] as EditText

            // Create the get current user info service
            val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
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
                            "commentToPostEditText" to commentToPostEditText,
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
            // Get the comment content to post edit text
            val commentToPostEditText = params[0]!!["commentToPostEditText"] as EditText

            // Get the comment content to post
            val commentContentToPost = params[0]!!["commentContentToPost"] as String

            // Get email of the writer (currently logged in user)
            val commentWriterEmail = params[0]!!["commentWriterEmail"] as String

            // Get post id
            val postId = params[0]!!["postId"] as String

            // Create the create comment service
            val postCommentService: CreateNewHBTGramPostCommentService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
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
                        Toast.makeText(activity, "Comment can't be posted", Toast.LENGTH_SHORT).show()
                    } else {
                        // Empty content of the EditText
                        commentToPostEditText.setText("")

                        // Reload the RecyclerView
                        hbtGram.reloadRecyclerView()
                    }
                }
            })

            return null
        }
    }

    // The function to take user to the activity where the user can see post detail
    fun gotoPostDetail (postObject: HBTGramPost) {
        // Create the intent object
        val intent = Intent(activity, HBTGramPostDetail::class.java)

        // Put the selected post object into the intent so that the activity will know which post to show detail of
        intent.putExtra("selectedPostObject", postObject)

        // Start the class group post detail activity
        activity.startActivity(intent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hbt_gram_post_item, parent, false)

        // Return the ViewHolder
        return ViewHolderHBTGramPost(view)
    }

    override fun getItemCount(): Int {
        // Return the number posts
        return hbtGramPostObjects.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Call the function to set up the post
        (holder as ViewHolderHBTGramPost).setUpPostInfo(hbtGramPostObjects[position])
    }
}