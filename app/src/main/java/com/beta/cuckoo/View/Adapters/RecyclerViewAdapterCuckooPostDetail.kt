package com.beta.cuckoo.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.cuckoo.Interfaces.CreateNotificationInterface
import com.beta.cuckoo.Model.CuckooPost
import com.beta.cuckoo.Model.PostComment
import com.beta.cuckoo.Model.PostPhoto
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.PostRepositories.PostRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.PostDetail.PostDetail
import com.beta.cuckoo.View.UserInfoView.ProfileDetail
import com.beta.cuckoo.View.UserInfoView.UserShow
import com.beta.cuckoo.View.ZoomImage
import com.bumptech.glide.Glide
import com.google.gson.Gson
import java.util.concurrent.ExecutorService

class RecyclerViewAdapterCuckooPostDetail (hbtGramPost: CuckooPost, arrayOfImages: ArrayList<PostPhoto>,
                                           arrayOfComments: ArrayList<PostComment>, hbtGramPostDetail: PostDetail,
                                           activity: Activity, createNotificationInterface: CreateNotificationInterface, executorService: ExecutorService) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // The post repository
    private val postRepository: PostRepository = PostRepository(executorService, activity)

    // The user info repository
    private val userInfoRepository: UserRepository = UserRepository(executorService, activity)

    // The executor service
    private val executorService = executorService

    // The selected HBTGram post object
    private val hbtGramPost = hbtGramPost

    // Activity of the parent activity
    private val hbtGramPostDetailActivity = hbtGramPostDetail

    // Create notification interface
    private val createNotificationInterface = createNotificationInterface

    // Array of images of the post
    private val arrayOfImages = arrayOfImages

    // Array of comments of the post
    private val arrayOfComments = arrayOfComments

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
        fun setUpPostDetailHeader (hbtGramPost: CuckooPost) {
            // Call the function to get info of the post writer
            getUserInfoBasedOnId(hbtGramPost.getWriter(), postWriterFullName, postWriterAvatar)

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
        fun setUpPostContent (hbtGramPost: CuckooPost) {
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

            // Set up on click listener for the photo so that it will take user to the activity where user can see zoomable image
            postPhoto.setOnClickListener {
                // Call the function which will take user to the activity where user can zoom image
                gotoZoom(imageURL)
            }
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
        fun setUpNumOfLikesAndComments (postObject: CuckooPost) {
            // Call the function to get like status between current user and the post
            getLikeStatus(postObject.getId(), likeButton)

            // Set up on click listener for the like button
            likeButton.setOnClickListener {
                // Call the function to create new like for the post and send notification to the post writer
                createNewLike(postObject.getWriter(), postObject.getId())
            }

            // Set on click listener for the number of likes so that it will take user to the activity
            // where the user can see list of users who liked the post
            numOfLikesTextView.setOnClickListener {
                // Take user to the activity where the user can see list of users who liked the post
                // The intent object
                val intent = Intent(hbtGramPostDetailActivity, UserShow::class.java)

                // Set post id to that activity and tell it to show list of likes of the post
                intent.putExtra("whatToDo", "getListOfLikes")
                intent.putExtra("postId", postObject.getId())
                intent.putExtra("userId", "")

                // Start the activity
                hbtGramPostDetailActivity.startActivity(intent)
            }

            // Call the function to get number of likes of the post
            getNumOfLikes(postObject.getId(), numOfLikesTextView)

            // Call the function to get number of comments of the post
            getNumOfComments(postObject.getId(), numOfCommentsTextView)
        }
    }

    // ViewHolder for comments of the post
    inner class ViewHolderPostComments internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val writerAvatar : ImageView = itemView.findViewById(R.id.commentWriterAvatarPostDetail)
        private val writerFullName : TextView = itemView.findViewById(R.id.commentWriterFullNamePostDetail)
        private val commentContent : TextView = itemView.findViewById(R.id.commentContentPostDetail)
        private val openCommentOptionsMenuButton : ImageView = itemView.findViewById(R.id.openCommentOptionsCommentWithoutPhoto)

        // The function to set up comment
        fun setUpComment (hbtGramPostComment: PostComment) {
            // Call the function to get info of the user
            getUserInfoBasedOnId(hbtGramPostComment.getCommentWriter(), writerFullName, writerAvatar)

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

            // Set on click listener for the open comment options menu button
            openCommentOptionsMenuButton.setOnClickListener {
                // Call the function to open the menu
                hbtGramPostDetailActivity.openCommentOptionsMenu(hbtGramPostComment.getIdComment())
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
        private val openCommentOptionsMenuButton: ImageView = itemView.findViewById(R.id.openCommentOptionsCommentWithPhoto)

        // The function to set up comment with photo
        fun setUpComment (hbtGramPostComment: PostComment) {
            // Call the function to get info of the comment writer
            getUserInfoBasedOnId(hbtGramPostComment.getCommentWriter(), writerFullName, writerAvatar)

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

            // Set on click listener for the open comment options menu button
            openCommentOptionsMenuButton.setOnClickListener {
                // Call the function to open the menu
                hbtGramPostDetailActivity.openCommentOptionsMenu(hbtGramPostComment.getIdComment())
            }
        }
    }

    // ViewHolder for blank comment section (maybe no comments)
    inner class ViewHolderNoComments internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // The function to set up blank row
        fun setUpBlankRow () {}
    }

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

    //*************************** GET USER INFO BASED ON ID ***************************
    // The function to get info of user based on id
    fun getUserInfoBasedOnId (userId: String, fullNameTextView: TextView, avatarImageView: ImageView) {
        // Call the function to get info of the post writer
        userInfoRepository.getUserInfoBasedOnId(userId) {userObject ->
            // Load full name into the TextView
            fullNameTextView.text = userObject.getFullName()

            // Load avatar info the ImageView
            Glide.with(hbtGramPostDetailActivity)
                .load(userObject.getAvatarURL())
                .into(avatarImageView)
        }
    }
    //*************************** END GET USER INFO BASED ON ID ***************************

    //*********************************** FUNCTION TO GET PHOTO OF THE COMMENT ***********************************
    // The function to get photo of the comment based on comment id
    fun getPhotoOfComment (commentId: String, commentPhotoImageView: ImageView) {
        // Call the function to get photo of the comment based on comment id
        postRepository.getPhotoOfComment(commentId) {commentPhotoURL: String ->
            Glide.with(hbtGramPostDetailActivity)
                .load(commentPhotoURL)
                .into(commentPhotoImageView)
        }
    }
    //*********************************** END FUNCTION TO GET PHOTO OF THE COMMENT ***********************************

    //*********************************** CREATE NEW LIKE SEQUENCE ***********************************
    // The function to create new like based on the specified user id (in this case, it gonna be id of the current user)
    fun createNewLike (likeReceiverId: String, postId: String) {
        // Call the function to create new like
        postRepository.createLikeForPost(postId) {likerId ->
            // Call the function to send notification
            createNotificationInterface.createNotification("liked", likeReceiverId, likerId, "", postId)
        }
    }
    //*********************************** END CREATE NEW LIKE SEQUENCE ***********************************

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
        val intent = Intent(hbtGramPostDetailActivity, ProfileDetail::class.java)

        // Update user object property of the profile detail activity
        intent.putExtra("selectedUserObject", userObject)

        // Start the activity
        hbtGramPostDetailActivity.startActivity(intent)
    }
    //******************************** END GET INFO OF USER BASED ON ID AND GO TO PROFILE DETAIL SEQUENCE ********************************

    //*********************************** ADDITIONAL FUNCTIONS ***********************************
    // The function which will take user to the activity where user can zoom in and out an image
    fun gotoZoom (imageURL: String) {
        if (imageURL == "") {
            return
        }

        // The intent object
        val intent = Intent(hbtGramPostDetailActivity, ZoomImage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Let the activity know which image to load
        intent.putExtra("imageURLToLoad", imageURL)

        // Start the activity
        hbtGramPostDetailActivity.startActivity(intent)
    }
    //*********************************** ADDITIONAL FUNCTIONS ***********************************

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
                val hbtGramPostPhotoModel = gs.fromJson<PostPhoto>(js, PostPhoto::class.java)

                // Call the function to set up view holder
                (holder as ViewHolderHBTGramPostPhotos).setUpPostPhoto(hbtGramPostPhotoModel.getImageURL())
            }
            // After that, show the number of comments and likes
            position == arrayOfImages.size + 2 -> {
                (holder as ViewHolderNumOfLikesAndComments).setUpNumOfLikesAndComments(hbtGramPost)
            }
            // The rest will show the comments
            else -> {
                if (arrayOfComments.size != 0 && arrayOfImages.size != 0) {
                    // Convert the arrayOfComments[position - 3 - arrayOfImages.size] object which is currently a linked tree map into a JSON string
                    val jsComments = gs.toJson(arrayOfComments[position - 3 - arrayOfImages.size])

                    // Convert the JSOn string back into HBTGramPostComment class
                    val hbtGramPostCommentModel = gs.fromJson<PostComment>(jsComments, PostComment::class.java)

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
                    val hbtGramPostCommentModel = gs.fromJson<PostComment>(jsComments, PostComment::class.java)

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