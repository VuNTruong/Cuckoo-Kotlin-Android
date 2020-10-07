package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetFirstImageURLOfPostService
import com.beta.myhbt_api.Controller.GetUserInfoService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterHBTGramPost (hbtGramPostObjects: ArrayList<HBTGramPost>, activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of HBTGram posts
    private val hbtGramPostObjects = hbtGramPostObjects

    // Activity of the parent activity
    private val activity = activity

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
    private inner class GetFirstPhotoTask: AsyncTask<HashMap<String, Any>, Void, Void>() {
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

                        // Get image info from the received data
                        val firstImageInfo = (data["documents"] as List<Map<String, Any>>)[0]

                        // Get URL of the image
                        val firstImageURL = firstImageInfo["imageURL"] as String

                        // Load that URL into the ImageView
                        Glide.with(activity)
                            .load(firstImageURL)
                            .into(postPhotoImageView)
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to load avatar of the currently logged in user
    private inner class LoadAvatarOfCurrentUserTask: AsyncTask<HashMap<String, Any>, Void, Void>() {
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