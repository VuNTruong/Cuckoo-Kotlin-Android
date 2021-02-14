package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Interfaces.PostShowingInterface
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.Repository.PostRepositories.PostRepository
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.beta.myhbt_api.View.HBTGramPostDetail
import com.beta.myhbt_api.View.ProfileDetail
import com.bumptech.glide.Glide
import com.google.gson.Gson

import java.util.concurrent.ExecutorService

class RecyclerViewAdapterHBTGramPost (hbtGramPostObjects: ArrayList<HBTGramPost>, activity: Activity,
                                      postsInterface: PostShowingInterface, executorService: ExecutorService, currentUserObject: User) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // The post repository
    private val postRepository: PostRepository = PostRepository(executorService, activity)

    // The user info repository
    private val userInfoRepository: UserRepository = UserRepository(executorService, activity)

    // Array of HBTGram posts
    private val hbtGramPostObjects = hbtGramPostObjects

    // Activity of the parent activity
    private val activity = activity

    // The parent activity
    private val postsInterface = postsInterface

    // User object of the currently logged in user
    private val currentUserObject = currentUserObject

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
            getLikeStatus(postObject.getId(), likeButton)

            // Set on click listener for the comment button so that it will take user to the activity where the
            // user can see post detail
            commentButton.setOnClickListener {
                // Call the function
                gotoPostDetail(postObject)
                activity.overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
            }

            // Set on click listener for the like button
            likeButton.setOnClickListener {
                // Call the function to create new like for the post
                createNewLike(postObject.getWriter(), postObject.getId())
            }

            // Set on click listener for the post comment button
            postCommentButton.setOnClickListener {
                // Call the function to create new comment for the post
                createNewComment(commentToPostContent, postObject.getId(), postObject.getWriter())
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
        // Call the function to get info of the post writer
        userInfoRepository.getUserInfoBasedOnId(userId) {userObject ->
            // Load full name into the TextView
            fullNameTextView.text = userObject.getFullName()

            // Load avatar info the ImageView
            Glide.with(activity)
                .load(userObject.getAvatarURL())
                .into(avatarImageView)
        }
    }
    //*************************** END GET INFO OF POST WRITER ***************************

    //*************************** GET FIRST PHOTO OF THE POST ***************************
    // The function to get first photo of the post
    fun getFirstPhoto (postId: String, postPhotoImageView: ImageView) {
        // Call the function to get first image of the post with the specified post id
        postRepository.getFirstPhotoOfPost(postId) {firstPhotoURL ->
            // Load photo into the image view
            Glide.with(activity)
                .load(firstPhotoURL)
                .into(postPhotoImageView)
        }
    }
    //*************************** END GET FIRST PHOTO OF THE POST ***************************

    //************************************* GET INFO OF CURRENT USER *************************************
    // The function to get info of the currently logged in user
    fun getCurrentUserInfo (userAvatar: ImageView) {
        // Load current user avatar into the image view
        Glide.with(activity)
            .load(currentUserObject.getAvatarURL())
            .into(userAvatar)
    }
    //************************************* END GET INFO OF CURRENT USER *************************************

    //************************************* GET NUMBER OF LIKES AND COMMENTS *************************************
    // The function to get number of comments of the post
    fun getNumOfComments (postId: String, numOfCommentsTextView: TextView) {
        // Call the function to get number of comments for the post
        postRepository.getNumberOfCommentsForPost(postId) {numOfComments ->
            // Load number of comments into the text view
            numOfCommentsTextView.text = "$numOfComments comments"
        }
    }

    // The function to get number of comments of the post
    fun getNumOfLikes (postId: String, numOfLikesTextView: TextView) {
        // Call the function to get number of likes for the post
        postRepository.getNumberOfLikesForPost(postId) {numOfLikes ->
            // Load number of likes into the text view
            numOfLikesTextView.text = "$numOfLikes likes"
        }
    }
    //************************************* END GET NUMBER OF LIKES AND COMMENTS *************************************

    //*********************************** CREATE NEW LIKE SEQUENCE ***********************************
    // The function to create new like based on the specified user id (in this case, it gonna be id of the current user)
    fun createNewLike (likeReceiverId: String, postId: String) {
        // Call the function to create new like
        postRepository.createLikeForPost(postId) {likerId ->
            // Call the function to send notification
            postsInterface.createNotification("liked", likeReceiverId, likerId, "", postId)
        }
    }
    //*********************************** END CREATE NEW LIKE SEQUENCE ***********************************

    //*********************************** CREATE NEW COMMENT SEQUENCE ***********************************
    // The function to create new comment for the post
    fun createNewComment (commentContentToPostEditText: EditText, postId: String, commentReceiverUserId: String) {
        // Call the function to create new comment for the post
        postRepository.createCommentForPost(commentContentToPostEditText.text.toString(), postId) {commentWriterId ->
            // Call the function to send notification
            postsInterface.createNotification("commented", commentReceiverUserId, commentWriterId, "", postId)
        }
    }
    //*********************************** END CREATE NEW COMMENT SEQUENCE ***********************************

    //*********************************** CHECK LIKE STATUS SEQUENCE ***********************************
    // The function to get like status
    fun getLikeStatus (postId: String, likeButton: CheckBox) {
        // Call the function to get like status between current user and the specified post
        postRepository.checkLikeStatusOfPostAndCurrentUser(postId) {liked ->
            // If like status is "Done. User has liked post", the user has liked the post and set like button to be the red heart
            // Otherwise, let it be the blank heart
            likeButton.isChecked = liked
        }
    }
    //*********************************** END CHECK LIKE STATUS SEQUENCE ***********************************

    //******************************** GET INFO OF USER BASED ON ID AND GO TO PROFILE DETAIL SEQUENCE ********************************
    // The function to get user info based on id
    fun getUserInfoBasedOnIdAndGotoProfileDetail(userId: String) {
        // Call the function to get info of the user based on id
        userInfoRepository.getUserInfoBasedOnId(userId) {userObject ->
            // Call the function to take user to the activity where user can see profile detail of the user
            gotoProfileDetail(userObject)
        }
    }

    // The function to take user to the activity where the user can see profile detail of user with specified id
    private fun gotoProfileDetail (userObject: User) {
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