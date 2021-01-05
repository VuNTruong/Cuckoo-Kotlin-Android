package com.beta.myhbt_api.View.Adapters

import android.animation.StateListAnimator
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Interfaces.LoadMorePostsInterface
import com.beta.myhbt_api.Interfaces.PostShowingInterface
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Fragments.DashboardFragment
import com.beta.myhbt_api.View.HBTGramPostDetail
import com.beta.myhbt_api.View.ProfileDetail
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterHBTGramPost (hbtGramPostObjects: ArrayList<HBTGramPost>, activity: Activity, postsInterface: PostShowingInterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of HBTGram posts
    private val hbtGramPostObjects = hbtGramPostObjects

    // Activity of the parent activity
    private val activity = activity

    // The parent activity
    private val postsInterface = postsInterface

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
        private val likeButton: CheckBox = itemView.findViewById(R.id.likeButton)
        private val commentButton: ImageView = itemView.findViewById(R.id.commentButton)
        private val userAvatar: ImageView = itemView.findViewById(R.id.userAvatar)
        private val commentToPostContent: EditText = itemView.findViewById(R.id.commentToPostContent)
        private var postCommentButton: ImageView = itemView.findViewById(R.id.postCommentButton)

        // Photo URL of the first photo
        private var firstPhotoURL: String = ""

        // The function to set up post info
        fun setUpPostInfo (postObject: HBTGramPost) {
            // Call the function to get info of the post writer
            getInfoOfPostWriter(postObject.getWriter(), writerFullName, postWriterAvatar)

            // Call the function to get first photo of the post
            getFirstPhoto(postObject.getId(), postPhoto)

            // Call the function to get avatar of the currently logged in user
            getCurrentUserInfo(userAvatar)

            // Call the function to get number of likes for the post
            getNumOfLikes(postObject.getId(), numOfLikes)

            // Call the function to get number of comments for the post
            getNumOfComments(postObject.getId(), numOfComments)

            // Call the function to set up like status for the like button
            getUserInfoAndGetLikeStatus(postObject.getId(), likeButton)

            // Set on click listener for the comment button so that it will take user to the activity where the
            // user can see post detail
            commentButton.setOnClickListener {
                // Call the function
                gotoPostDetail(postObject)
            }

            // Set on click listener for the like button
            likeButton.setOnClickListener {
                // Call the function to create new like for the post which is liked by the current user
                getUserInfoAndCreateNewLike(postObject.getId(), postObject.getWriter())
            }

            // Set on click listener for the post comment button
            postCommentButton.setOnClickListener {
                // Call the function to get info of the current user and create new comment of that user
                getUserInfoAndCreateComment(commentToPostContent, postObject.getId())
            }

            // Set on click listener for the user full name text view and avatar image view
            // so that it will take user to the activity where the user can see profile detail
            // of the post writer
            writerFullName.setOnClickListener {
                // Call the function
                getUserInfoBasedOnIdAndGotoProfileDetail(postObject.getWriter())
            }
            postWriterAvatar.setOnClickListener{
                // Call the function
                getUserInfoBasedOnIdAndGotoProfileDetail(postObject.getWriter())
            }

            // Load other info
            dateCreated.text = postObject.getDateCreated()
            postContent.text = postObject.getContent()
        }
    }

    // ViewHolder for the load more post row
    inner class ViewHolderHBTGramLoadMorePost internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val loadMorePostLayout: ConstraintLayout = itemView.findViewById(R.id.loadMorePostLayout)

        // The function to set up the load more posts row
        fun setUpLoadMorePostRow () {
            // Set on click listener for the load more post layout
            loadMorePostLayout.setOnClickListener {
                // Call the function in the fragment to load more posts
                postsInterface.loadMorePosts()
            }
        }
    }

    //*************************** GET INFO OF POST WRITER ***************************
    // The function to get info of the post writer
    fun getInfoOfPostWriter (userId: String, fullNameTextView: TextView, avatarImageView: ImageView) {
        // Create the get user info based on id service
        val getUserInfoBasedOnIdService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(GetUserInfoBasedOnIdService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserInfoBasedOnIdService.getUserInfoBasedOnId(userId)

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

                    // Get user info from the received data
                    val userInfo = (data["documents"] as List<Map<String, Any>>)[0]

                    // Get full name of the user
                    val fullName = userInfo["fullName"] as String

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
    }
    //*************************** END GET INFO OF POST WRITER ***************************

    //*************************** GET FIRST PHOTO OF THE POST ***************************
    // The function to get first photo of the post
    fun getFirstPhoto (postId: String, postPhotoImageView: ImageView) {
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
    }
    //*************************** END GET FIRST PHOTO OF THE POST ***************************

    //************************************* GET INFO OF CURRENT USER *************************************
    // The function to get info of the currently logged in user
    fun getCurrentUserInfo (userAvatar: ImageView) {
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
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get avatar URL of the user
                    val avatarURL = data["avatarURL"] as String

                    // Load that avatar URL into the ImageView
                    Glide.with(activity)
                        .load(avatarURL)
                        .into(userAvatar)
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //************************************* END GET INFO OF CURRENT USER *************************************

    //************************************* GET NUMBER OF LIKES AND COMMENTS *************************************
    // The function to get number of comments of the post
    fun getNumOfComments (postId: String, numOfCommentsTextView: TextView) {
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
    }

    // The function to get number of comments of the post
    fun getNumOfLikes (postId: String, numOfLikesTextView: TextView) {
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
    }
    //************************************* END GET NUMBER OF LIKES AND COMMENTS *************************************

    //*********************************** CREATE NEW LIKE SEQUENCE ***********************************
    // The function which will get info of the current user and create new like based on that info
    fun getUserInfoAndCreateNewLike (postId: String, likeReceiverId: String) {
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

                    // Get user id of the current user
                    val userId = data["_id"] as String

                    // Call the function to add new like based on user id of the current user
                    createNewLike(userId, postId, likeReceiverId)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to create new like based on the specified user id (in this case, it gonna be id of the current user)
    fun createNewLike (likerId: String, likeReceiverId: String, postId: String) {
        // Create the add like service
        val addLikeService: CreateNewHBTGramPostLikeService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            CreateNewHBTGramPostLikeService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = addLikeService.createNewHBTGramPostLike(likerId, postId)

        // Perform the API call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // Reload number of likes
                this@RecyclerViewAdapterHBTGramPost.notifyDataSetChanged()

                // Call the function to send notification
                postsInterface.createNotification("liked", likeReceiverId, likerId, "", postId)
            }
        })
    }
    //*********************************** END CREATE NEW LIKE SEQUENCE ***********************************

    //*********************************** CREATE NEW COMMENT SEQUENCE ***********************************
    // The function to get user id of the current user and create comment based on that
    fun getUserInfoAndCreateComment (commentToPostContentEditText: EditText, postId: String) {
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
        val postCommentService: CreateNewHBTGramPostCommentService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
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
                    Toast.makeText(activity, "Comment can't be posted", Toast.LENGTH_SHORT).show()
                } else {
                    // Empty content of the EditText
                    commentContentToPostEditText.setText("")
                }
            }
        })
    }
    //*********************************** END CREATE NEW COMMENT SEQUENCE ***********************************

    //*********************************** CHECK LIKE STATUS SEQUENCE ***********************************
    /*
    In this sequence, we will do 2 things
    1. Get info of the current user
    2. Get like status between the current user and post at this row
     */

    // The function to get info of the current user
    fun getUserInfoAndGetLikeStatus (postId: String, likeButton: CheckBox) {
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
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user id in the database of the currently logged in user
                    val userId = data["_id"] as String

                    // Call the function to get like status between current user and post at this row
                    getLikeStatus(userId, postId, likeButton)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to get like status
    fun getLikeStatus (whoLike: String, postId: String, likeButton: CheckBox) {
        // Create the check like status service
        val checkHBTGramListStatusService: CheckHBTGramPostLikeStatusService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            CheckHBTGramPostLikeStatusService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = checkHBTGramListStatusService.checkHBTGramPostLikeStatus(whoLike, postId)

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
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get like status from body of the response
                    val likeStatus = responseBody["status"] as String

                    // If like status is "Done. User has liked post", the user has liked the post and set like button to be the red heart
                    // Otherwise, let it be the blank heart
                    likeButton.isChecked = likeStatus == "Done. User has liked post"
                }
            }
        })
    }
    //*********************************** END CHECK LIKE STATUS SEQUENCE ***********************************

    //******************************** GET INFO OF USER BASED ON ID AND GO TO PROFILE DETAIL SEQUENCE ********************************
    // The function to get user info based on id
    fun getUserInfoBasedOnIdAndGotoProfileDetail(userId: String) {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // Create the get user info service
        val getUserInfoService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetUserInfoBasedOnIdService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserInfoService.getUserInfoBasedOnId(userId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is data
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get obtain user data from that
                    val data = (((responseBody["data"] as Map<String, Any>)["documents"]) as List<Map<String, Any>>)[0]

                    // Convert user object which is currently a linked tree map into a JSON string
                    val jsUser = gs.toJson(data)

                    // Convert the JSOn string back into User class
                    val userObject = gs.fromJson<User>(jsUser, User::class.java)

                    // Call the function to take user to the activity where user can see profile detail of the user
                    gotoProfileDetail(userObject)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to take user to the activity where the user can see profile detail of user with specified id
    fun gotoProfileDetail (userObject: User) {
        // The intent object
        val intent = Intent(activity, ProfileDetail::class.java)

        // Update user object property of the profile detail activity
        intent.putExtra("selectedUserObject", userObject)

        // Start the activity
        activity.startActivity(intent)
    }
    //******************************** END GET INFO OF USER BASED ON ID AND GO TO PROFILE DETAIL SEQUENCE ********************************

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
        val view: View

        // If view type is 0, show the post
        return if (viewType == 0) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.hbt_gram_post_item, parent, false)

            // Return the ViewHolder
            ViewHolderHBTGramPost(view)
        } // If it is 1, show the load more row
        else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.hbt_gram_post_item_load_more, parent, false)

            // Return the ViewHolder
            ViewHolderHBTGramLoadMorePost(view)
        }
    }

    override fun getItemCount(): Int {
        // Return the number posts
        // Also add 1 because there is a load more button at the end
        return hbtGramPostObjects.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // From first row to number of elements in the array of post, show the post
        if (position in 0 until hbtGramPostObjects.size) {
            // In order to prevent us from encountering the class cast exception, we need to do the following
            // Create the GSON object
            val gs = Gson()

            // Convert the hbtGramPostObjects[position] object which is currently a linked tree map into a JSON string
            val js = gs.toJson(hbtGramPostObjects[position])

            // Convert the JSOn string back into HBTGramPost class
            val hbtGramPostModel = gs.fromJson<HBTGramPost>(js, HBTGramPost::class.java)

            // Call the function to set up the post
            (holder as ViewHolderHBTGramPost).setUpPostInfo(hbtGramPostModel)
        } // Last row will show the load more button
        else {
            // Call the function to set up the load more button
            (holder as ViewHolderHBTGramLoadMorePost).setUpLoadMorePostRow()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            in 0 until hbtGramPostObjects.size -> {
                // From first row to number of elements in the array of post, show the post
                0
            }
            else -> {
                // Last row will show the load more button
                1
            }
        }
    }
}