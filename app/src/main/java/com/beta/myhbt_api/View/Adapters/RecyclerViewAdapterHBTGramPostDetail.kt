package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.HBTGramPostComment
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.HBTGramPostDetail
import com.beta.myhbt_api.View.ProfileDetail
import com.beta.myhbt_api.View.UserShow
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterHBTGramPostDetail (hbtGramPost: HBTGramPost, arrayOfImages: ArrayList<HBTGramPostPhoto>, numOfComments: Int, numOfLikes: Int,
                                           arrayOfComments: ArrayList<HBTGramPostComment>, hbtGramPostDetail: HBTGramPostDetail) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // The selected HBTGram post object
    private val hbtGramPost = hbtGramPost

    // Activity of the parent activity
    private val hbtGramPostDetailActivity = hbtGramPostDetail

    // Array of images of the post
    private val arrayOfImages = arrayOfImages

    // Array of comments of the post
    private val arrayOfComments = arrayOfComments

    // Number of comments and likes
    private val numOfLikes = numOfLikes
    private val numOfComments = numOfComments

    // In order to prevent us from encountering the class cast exception, we need to do the following
    // Create the GSON object
    val gs = Gson()

    // ViewHolder for the post detail header
    inner class ViewHolderHBTGramPostDetailHeader internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val postWriterAvatar : ImageView = itemView.findViewById(R.id.writerAvatarPostDetail)
        private val postWriterFullName : TextView = itemView.findViewById(R.id.writerFullNamePostDetail)
        private val dateCreated : TextView = itemView.findViewById(R.id.dateCreatedPostDetail)

        // The function to set up post detail header
        fun setUpPostDetailHeader (hbtGramPost: HBTGramPost) {
            // Call the function to get info of the post writer
            getUserInfo(hbtGramPost.getWriter(), postWriterFullName, postWriterAvatar)

            // Load date created into the TextView
            dateCreated.text = hbtGramPost.getDateCreated()

            // Set on click listener for the full name text view and avatar image view
            // of the post writer so that it will take user to the activity where the user
            // can see profile detail of the post writer
            postWriterAvatar.setOnClickListener{
                // Call the function
                getUserInfoBasedOnIdAndGotoProfileDetail(hbtGramPost.getWriter())
            }
            postWriterFullName.setOnClickListener {
                // Call the function
                getUserInfoBasedOnIdAndGotoProfileDetail(hbtGramPost.getWriter())
            }
        }
    }

    // ViewHolder for the post content
    inner class ViewHolderHBTGramPostDetailPostContent internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val postContent : TextView = itemView.findViewById(R.id.postContentPostDetail)

        // The function to set up post content
        fun setUpPostContent (hbtGramPost: HBTGramPost) {
            // Load post detail into the TextView
            postContent.text = hbtGramPost.getContent()
        }
    }

    // ViewHolder for the post photos
    inner class ViewHolderHBTGramPostPhotos internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val postPhoto : ImageView = itemView.findViewById(R.id.postPhotoPostDetail)

        // The function to set up post photo
        fun setUpPostPhoto (imageURL: String) {
            // Load that image into the ImageView
            Glide.with(hbtGramPostDetailActivity)
                .load(imageURL)
                .into(postPhoto)
        }
    }

    // ViewHolder for the number of likes and comments
    inner class ViewHolderNumOfLikesAndComments internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val numOfLikesTextView : TextView = itemView.findViewById(R.id.numOfLikesPostDetail)
        private val numOfCommentsTextView : TextView = itemView.findViewById(R.id.numOfCommentsPostDetail)
        private val likeButton : CheckBox = itemView.findViewById(R.id.likeButtonPostDetail)

        // The function to set up number of likes and comments for the post
        fun setUpNumOfLikesAndComments (numOfLikes: Int, numOfComments: Int, postId: String) {
            // Call the function to get like status between current user and the post
            getUserInfoAndGetLikeStatus(postId, likeButton)

            // Set up on click listener for the like button
            likeButton.setOnClickListener {
                // Call the function to create new like for the post
                getUserInfoAndCreateNewLike(postId)
            }

            // Set on click listener for the number of likes so that it will take user to the activity
            // where the user can see list of users who liked the post
            numOfLikesTextView.setOnClickListener {
                // Take user to the activity where the user can see list of users who liked the post
                // The intent object
                val intent = Intent(hbtGramPostDetailActivity, UserShow::class.java)

                // Set post id to that activity and tell it to show list of likes of the post
                intent.putExtra("whatToDo", "getListOfLikes")
                intent.putExtra("postId", postId)
                intent.putExtra("userId", "")

                // Start the activity
                hbtGramPostDetailActivity.startActivity(intent)
            }

            // Load number of comments and likes into text view
            numOfLikesTextView.text = "$numOfLikes Likes"
            numOfCommentsTextView.text = "$numOfComments Comments"
        }
    }

    // ViewHolder for comments of the post
    inner class ViewHolderPostComments internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val writerAvatar : ImageView = itemView.findViewById(R.id.commentWriterAvatarPostDetail)
        private val writerFullName : TextView = itemView.findViewById(R.id.commentWriterFullNamePostDetail)
        private val commentContent : TextView = itemView.findViewById(R.id.commentContentPostDetail)

        // The function to set up comment
        fun setUpComment (hbtGramPostComment: HBTGramPostComment) {
            // Call the function to get info of the user
            getUserInfo(hbtGramPostComment.getCommentWriter(), writerFullName, writerAvatar)

            // Load content of the comment into the TextView
            commentContent.text = hbtGramPostComment.getCommentContent()

            // Set on click listener for the full name text view and avatar image view of the post writer
            // so that it will take user to the activity where the user can see profile detail of the comment
            // writer
            writerAvatar.setOnClickListener {
                // Call the function
                getUserInfoBasedOnIdAndGotoProfileDetail(hbtGramPostComment.getCommentWriter())
            }
            writerFullName.setOnClickListener {
                // Call the function
                getUserInfoBasedOnIdAndGotoProfileDetail(hbtGramPostComment.getCommentWriter())
            }
        }
    }

    // ViewHolder for comments of the post with photos
    inner class ViewHolderPostCommentsWithPhoto internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val writerAvatar : ImageView = itemView.findViewById(R.id.commentWriterAvatarPostDetailCommentWithPhoto)
        private val writerFullName : TextView = itemView.findViewById(R.id.commentWriterFullNamePostDetailCommentWithPhoto)
        private val commentPhoto : ImageView = itemView.findViewById(R.id.commentPhotoPostDetailCommentWithPhoto)

        // The function to set up comment with photo
        fun setUpComment (hbtGramPostComment: HBTGramPostComment) {
            // Call the function to get info of the comment writer
            getUserInfo(hbtGramPostComment.getCommentWriter(), writerFullName, writerAvatar)

            // Call the function to get photo of the comment
            getPhotoOfComment(hbtGramPostComment.getIdComment(), commentPhoto)

            // Set on click listener for the full name text view and avatar image view of the post writer
            // so that it will take user to the activity where the user can see profile detail of the comment
            // writer
            writerAvatar.setOnClickListener {
                // Call the function
                getUserInfoBasedOnIdAndGotoProfileDetail(hbtGramPostComment.getCommentWriter())
            }
            writerFullName.setOnClickListener {
                // Call the function
                getUserInfoBasedOnIdAndGotoProfileDetail(hbtGramPostComment.getCommentWriter())
            }
        }
    }


    // ViewHolder for blank comment section (maybe no comments)
    inner class ViewHolderNoComments internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // The function to set up blank row
        fun setUpBlankRow () {}
    }

    //******************************* GET INFO OF USER BASED ON ID *******************************
    // The function to get user info based on user id
    fun getUserInfo (userId: String, fullNameTextView: TextView, avatarImageView: ImageView) {
        // Create the get user info based on id service
        val getUserInfoBasedOnIdService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(hbtGramPostDetailActivity)!!.create(GetUserInfoBasedOnIdService::class.java)

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

                    // Get user full name
                    val userFullName = userInfo["fullName"] as String

                    // Get avatar URL of the user
                    val avatarURL = userInfo["avatarURL"] as String

                    // Load full name into the text view
                    fullNameTextView.text = userFullName

                    // Load avatar into the image view
                    Glide.with(hbtGramPostDetailActivity)
                        .load(avatarURL)
                        .into(avatarImageView)
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //******************************* END GET INFO OF USER BASED ON ID *******************************

    //*********************************** FUNCTION TO GET PHOTO OF THE COMMENT ***********************************
    // The function to get photo of the comment based on comment id
    fun getPhotoOfComment (commentId: String, commentPhotoImageView: ImageView) {
        // Create the get photo of comment service
        val getHBTGramPostCommentPhotoService: GetHBTGramPostCommentPhotoService = RetrofitClientInstance.getRetrofitInstance(hbtGramPostDetailActivity)!!.create(GetHBTGramPostCommentPhotoService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getHBTGramPostCommentPhotoService.getPostCommentPhoto(commentId)

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

                    // Get comment photo info from the received data
                    val photoInfo = (data["documents"] as List<Map<String, Any>>)[0]

                    // Get image URL of comment photo
                    val commentPhotoImageURL = photoInfo["imageURL"] as String

                    // Load photo of comment into the image view
                    Glide.with(hbtGramPostDetailActivity)
                        .load(commentPhotoImageURL)
                        .into(commentPhotoImageView)
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //*********************************** END FUNCTION TO GET PHOTO OF THE COMMENT ***********************************

    //*********************************** CREATE NEW LIKE SEQUENCE ***********************************
    // The function which will get info of the current user and create new like based on that info
    fun getUserInfoAndCreateNewLike (postId: String) {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(hbtGramPostDetailActivity)!!.create(
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

                    // Get user id of the current user
                    val userId = data["_id"] as String

                    // Call the function to add new like based on user id of the current user
                    createNewLike(userId, postId)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to create new like based on the specified user id (in this case, it gonna be id of the current user)
    fun createNewLike (likerId: String, postId: String) {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // Create the add like service
        val addLikeService: CreateNewHBTGramPostLikeService = RetrofitClientInstance.getRetrofitInstance(hbtGramPostDetailActivity)!!.create(
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
                this@RecyclerViewAdapterHBTGramPostDetail.notifyDataSetChanged()

                // Convert photo object which is currently a linked tree map into a JSON string
                val jsPhoto = gs.toJson(arrayOfImages[0])

                // Convert the JSOn string back into PostPhoto class
                val photoObject = gs.fromJson<HBTGramPostPhoto>(jsPhoto, HBTGramPostPhoto::class.java)

                // Call the function to send notification
                hbtGramPostDetailActivity.createNotification("liked", hbtGramPost.getWriter(), likerId, photoObject.getImageURL(), hbtGramPost.getId())
            }
        })
    }
    //*********************************** END CREATE NEW LIKE SEQUENCE ***********************************

    //*********************************** CHECK LIKE STATUS SEQUENCE ***********************************
    /*
    In this sequence, we will do 2 things
    1. Get info of the current user
    2. Get like status between the current user and post at this row
     */

    // The function to get info of the current user
    fun getUserInfoAndGetLikeStatus (postId: String, likeButton: CheckBox) {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(hbtGramPostDetailActivity)!!.create(
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
        val checkHBTGramListStatusService: CheckHBTGramPostLikeStatusService = RetrofitClientInstance.getRetrofitInstance(hbtGramPostDetailActivity)!!.create(
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
                    Toast.makeText(hbtGramPostDetailActivity, "Something is not right", Toast.LENGTH_SHORT).show()
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
        val getUserInfoService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(hbtGramPostDetailActivity)!!.create(
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
        val intent = Intent(hbtGramPostDetailActivity, ProfileDetail::class.java)

        // Update user object property of the profile detail activity
        intent.putExtra("selectedUserObject", userObject)

        // Start the activity
        hbtGramPostDetailActivity.startActivity(intent)
    }
    //******************************** END GET INFO OF USER BASED ON ID AND GO TO PROFILE DETAIL SEQUENCE ********************************



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view : View

        // Return the right view holder based on view type
        when(viewType) {
            // View type 0 is for the header
            0 -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.hbt_gram_post_detail_header, parent, false)
                return ViewHolderHBTGramPostDetailHeader(view)
            }
            // View type 1 is for the post content
            1 -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.hbt_gram_post_detail_post_content, parent, false)
                return ViewHolderHBTGramPostDetailPostContent(view)
            }
            // View type 2 is for the post photo
            2 -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.hbt_gram_post_detail_post_photo, parent, false)
                return ViewHolderHBTGramPostPhotos(view)
            }
            // View type 3 is for the number of comments and likes
            3 -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.hbt_gram_post_detail_num_of_likes_and_comments, parent, false)
                return ViewHolderNumOfLikesAndComments(view)
            }
            // View type 4 is for the comments
            4 -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.hbt_gram_post_detail_comment, parent, false)
                return ViewHolderPostComments(view)
            } // view type 5 is for the comments with photo
            5 -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.hbt_gram_post_detail_comment_with_photo, parent, false)
                return ViewHolderPostCommentsWithPhoto(view)
            }
            // View type 6 is for the blank row
            else -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.hbt_gram_post_detail_no_comments, parent, false)
                return ViewHolderNoComments(view)
            }
        }
    }

    override fun getItemCount(): Int {
        // Number of rows will be
        /*
        + header
        + content
        + photos
        + num of likes and comments
        + comments
         */
        return 3 + arrayOfComments.size + arrayOfImages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            // Position 0 will show the header
            position == 0 -> {
                (holder as ViewHolderHBTGramPostDetailHeader).setUpPostDetailHeader(hbtGramPost)
            }
            // Position 1 will show the post detail
            position == 1 -> {
                (holder as ViewHolderHBTGramPostDetailPostContent).setUpPostContent(hbtGramPost)
            }
            // From position 2, start showing the photos
            position >= 2 && position <= arrayOfImages.size + 1 -> {
                // Convert the arrayOfImages[position - 2] object which is currently a linked tree map into a JSON string
                val js = gs.toJson(arrayOfImages[position - 2])

                // Convert the JSOn string back into HBTGramPostPhoto class
                val hbtGramPostPhotoModel = gs.fromJson<HBTGramPostPhoto>(js, HBTGramPostPhoto::class.java)

                // Call the function to set up view holder
                (holder as ViewHolderHBTGramPostPhotos).setUpPostPhoto(hbtGramPostPhotoModel.getImageURL())
            }
            // After that, show the number of comments and likes
            position == arrayOfImages.size + 2 -> {
                (holder as ViewHolderNumOfLikesAndComments).setUpNumOfLikesAndComments(numOfLikes, numOfComments, hbtGramPost.getId())
            }
            // The rest will show the comments
            else -> {
                if (arrayOfComments.size != 0 && arrayOfImages.size != 0) {
                    // Convert the arrayOfComments[position - 3 - arrayOfImages.size] object which is currently a linked tree map into a JSON string
                    val jsComments = gs.toJson(arrayOfComments[position - 3 - arrayOfImages.size])

                    // Convert the JSOn string back into HBTGramPostComment class
                    val hbtGramPostCommentModel = gs.fromJson<HBTGramPostComment>(jsComments, HBTGramPostComment::class.java)

                    // Check to see if comment has photo or not
                    if (hbtGramPostCommentModel.getCommentContent() == "image") {
                        // If content of the comment is "image", let the row show comment with photo
                        // Call the function to set up the view holder
                        (holder as ViewHolderPostCommentsWithPhoto).setUpComment(hbtGramPostCommentModel)
                    } // Otherwise, let the row show comment without photo
                    else {
                        // Call the function to set up the view holder
                        (holder as ViewHolderPostComments).setUpComment(hbtGramPostCommentModel)
                    }
                } else {
                    (holder as ViewHolderNoComments).setUpBlankRow()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            // Position 0 will show the header
            position == 0 -> {
                0
            }
            // Position 1 will show the post detail
            position == 1 -> {
                1
            }
            // From position 2, start showing the photos
            position >= 2 && position <= arrayOfImages.size + 1 -> {
                2
            }
            // After that, show the number of comments and likes
            position == arrayOfImages.size + 2 -> {
                3
            }
            // The rest will show the comments
            else -> {
                if (arrayOfComments.size != 0 && arrayOfImages.size != 0) {
                    // Convert the arrayOfComments[position - 3 - arrayOfImages.size] object which is currently a linked tree map into a JSON string
                    val jsComments = gs.toJson(arrayOfComments[position - 3 - arrayOfImages.size])

                    // Convert the JSOn string back into HBTGramPostComment class
                    val hbtGramPostCommentModel = gs.fromJson<HBTGramPostComment>(jsComments, HBTGramPostComment::class.java)

                    // Check to see if comment has photo or not
                    if (hbtGramPostCommentModel.getCommentContent() == "image") {
                        5
                    } // Otherwise, let the row show comment without photo
                    else {
                        4
                    }
                } else {
                    6
                }
            }
        }
    }
}