package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.GetUserInfoService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.HBTGramPostComment
import com.beta.myhbt_api.R
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterHBTGramPostDetail (hbtGramPost: HBTGramPost, arrayOfImages: ArrayList<String>,
                                           arrayOfComments: ArrayList<HBTGramPostComment>, activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // The selected HBTGram post object
    private val hbtGramPost = hbtGramPost

    // Activity of the parent activity
    private val activity = activity

    // Array of images of the post
    private val arrayOfImages = arrayOfImages

    // Array of comments of the post
    private val arrayOfComments = arrayOfComments

    // ViewHolder for the post detail header
    inner class ViewHolderHBTGramPostDetailHeader internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val postWriterAvatar : ImageView = itemView.findViewById(R.id.writerAvatarPostDetail)
        private val postWriterFullName : TextView = itemView.findViewById(R.id.writerFullNamePostDetail)
        private val dateCreated : TextView = itemView.findViewById(R.id.dateCreatedPostDetail)

        // The function to set up post detail header
        fun setUpPostDetailHeader (hbtGramPost: HBTGramPost) {
            // Execute the AsyncTask to get info of the post writer
            GetUserInfoTask().execute(hashMapOf(
                "writerEmail" to hbtGramPost.getWriter(),
                "fullNameTextView" to postWriterFullName,
                "avatarImageView" to postWriterAvatar
            ))

            // Load date created into the TextView
            dateCreated.text = hbtGramPost.getDateCreated()
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
            Glide.with(activity)
                .load(imageURL)
                .into(postPhoto)
        }
    }

    // ViewHolder for the number of likes and comments
    inner class ViewHolderNumOfLikesAndComments internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val numOfLikes : TextView = itemView.findViewById(R.id.numOfLikesPostDetail)
        private val numOfComments : TextView = itemView.findViewById(R.id.numOfCommentsPostDetail)
        private val likeButton : ImageView = itemView.findViewById(R.id.likeButtonPostDetail)

        // The function to set up number of likes and comments for the post
        fun setUpNumOfLikesAndComments () {
            // Set up on click listener fot the like button
            likeButton.setOnClickListener {  }
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
            // Execute the AsyncTask to set up full name and avatar for the comment writer
            GetUserInfoTask().execute(hashMapOf(
                "writerEmail" to hbtGramPostComment.getCommentWriter(),
                "fullNameTextView" to writerFullName,
                "avatarImageView" to writerAvatar
            ))

            // Load content of the comment into the TextView
            commentContent.text = hbtGramPostComment.getCommentContent()
        }
    }

    // ViewHolder for blank comment section (maybe no comments)
    inner class ViewHolderNoComments internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // The function to set up blank row
        fun setUpBlankRow () {}
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
            val getUserInfoService: GetUserInfoService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
                GetUserInfoService::class.java)

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
            } // View type 5 is for the blank row
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
                (holder as ViewHolderHBTGramPostPhotos).setUpPostPhoto(arrayOfImages[position - 2])
            }
            // After that, show the number of comments and likes
            position == arrayOfImages.size + 2 -> {
                (holder as ViewHolderNumOfLikesAndComments).setUpNumOfLikesAndComments()
            }
            // The rest will show the comments
            else -> {
                if (arrayOfComments.size != 0 && arrayOfImages.size != 0) {
                    (holder as ViewHolderPostComments).setUpComment(arrayOfComments[position - 3 - arrayOfImages.size])
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
                    4
                } else {
                    5
                }
            }
        }
    }
}