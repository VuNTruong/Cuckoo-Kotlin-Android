package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.GetPostBasedOnIdService
import com.beta.myhbt_api.Controller.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Interfaces.LoadMorePostsInterface
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.Notification
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.HBTGramPostDetail
import com.beta.myhbt_api.View.ProfileDetail
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterNotification (arrayOfNotifications: ArrayList<Notification>, activity: Activity, loadMorePostsInterface: LoadMorePostsInterface) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of notifications
    private val arrayOfNotifications = arrayOfNotifications

    // Parent activity
    private val activity = activity

    // The load more posts interface
    private val loadMorePostsInterface = loadMorePostsInterface

    //*********************************** VIEW HOLDERS FOR THE RECYCLER VIEW ***********************************
    // ViewHolder for the notification row
    inner class ViewHolderNotificationRow internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val notificationAvatar: ImageView = itemView.findViewById(R.id.notificationAvatar)
        private val notificationContent: TextView = itemView.findViewById(R.id.notificationContent)
        private val notificationContentView: ConstraintLayout = itemView.findViewById(R.id.notificationContentView)
        private val notificationImage: ImageView = itemView.findViewById(R.id.notificationImage)

        // The function to set up the notification row
        fun setUpNotificationRow (notification: Notification) {
            // Call the function to set up avatar for the user that associated with the notification
            // as well as loading the right thing for the content
            getUserInfoBasedOnId(notification.getFromUser(), notificationAvatar, notificationContent, notification.getContent())

            // Load image for the notification
            Glide.with(activity)
                .load(notification.getImage())
                .into(notificationImage)

            // Set on click listener for the notification content view
            notificationContentView.setOnClickListener {
                // If the post id is "none", take user to the activity where user can see profile detail of user associated with the notification
                if (notification.getId() == "none") {
                    // Call the function
                    getUserInfoBasedOnIdAndGotoProfileDetail(notification.getFromUser())
                } else {
                    // Call the function which will take user to the activity where user can see post detail of the post associated with the notification
                    getPostObjectBasedOnIdAndGotoPostDetail(notification.getPostId())
                }
            }

            // Set on click listener for the notification avatar
            notificationAvatar.setOnClickListener{
                // Call the function which will take user to the activity where user can see profile detail of the user associated with the notification
                getUserInfoBasedOnIdAndGotoProfileDetail(notification.getFromUser())
            }
        }
    }

    // ViewHolder for the load more notifications row
    inner class ViewHolderHBTGramLoadMoreNotifications internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val loadMorePostLayout: ConstraintLayout = itemView.findViewById(R.id.loadMorePostLayout)

        // The function to set up the load more notifications row
        fun setUpLoadMoreNotificationRow () {
            // Set on click listener for the load more post layout
            loadMorePostLayout.setOnClickListener {
                // Call the function to load more notifications
                loadMorePostsInterface.loadMorePosts()
            }
        }
    }

    //*************************** GET USER INFO BASED ON ID AND LOAD NOTIFICATION CONTENT SEQUENCE ***************************
    // The function to get user info based on id and load content for notification
    fun getUserInfoBasedOnId(userId: String, notificationAvatar: ImageView, notificationContentTextView: TextView, notificationContent: String) {
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

                    // Get data from the response body
                    // Get obtain user data from that
                    val data = (((responseBody["data"] as Map<String, Any>)["documents"]) as List<Map<String, Any>>)[0]

                    // Get avatar URL of the user
                    val userAvatarURL = data["avatarURL"] as String

                    // Get full name of the user
                    val userFullName = data["fullName"] as String

                    // Load avatar into the image view
                    Glide.with(activity)
                        .load(userAvatarURL)
                        .into(notificationAvatar)

                    // Call the function to load content for notification
                    loadContentForNotification(notificationContentTextView, userFullName, notificationContent)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to load content for notification based on content from database
    fun loadContentForNotification (notificationContentTextView: TextView, userFullName: String, content: String) {
        // Based on content of notification from database to load the right thing
        when (content) {
            "liked" -> {
                notificationContentTextView.text = "$userFullName has liked your post"
            }
            "commented" -> {
                notificationContentTextView.text = "$userFullName has commented on your post"
            }
            "followed" -> {
                notificationContentTextView.text = "$userFullName started following you"
            }
        }
    }
    //*************************** GET USER INFO BASED ON ID AND LOAD NOTIFICATION CONTENT SEQUENCE ***************************

    //*********************************** GO TO POST DETAIL SEQUENCE ***********************************
    /*
    In this sequence, we will do 2 things
    1. Get post object based on the specified id
    2. Go to the post detail activity
     */

    // The function to get post object based on the specified post id
    fun getPostObjectBasedOnIdAndGotoPostDetail (postId: String) {
        // If the post detail is empty, get out of the sequence
        if (postId == "") {
            return
        }

        // Create the get post based on id service
        val getPostBasedOnIdService: GetPostBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetPostBasedOnIdService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getPostBasedOnIdService.getPostBasedOnId(postId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("There seem be be an error ${t.stackTrace}")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response
                    val data = (((responseBody["data"] as Map<String, Any>)["documents"]) as ArrayList<Map<String, Any>>)[0]

                    // In order to prevent us from encountering the class cast exception, we need to do the following
                    // Create the GSON object
                    val gs = Gson()

                    // Convert a linked tree map into a JSON string
                    val jsPost = gs.toJson(data)

                    // Convert the JSOn string back into HBTGramPost class
                    val hbtGramPostModel = gs.fromJson<HBTGramPost>(jsPost, HBTGramPost::class.java)

                    // Call the function which will take user to the activity where the user can see post detail of the post with specified id
                    gotoPostDetail(hbtGramPostModel)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function which will take user to the activity where the user can see post detail of the post with specified user id
    fun gotoPostDetail (postObject: HBTGramPost) {
        // Intent object
        val intent = Intent(activity, HBTGramPostDetail::class.java)

        // Pass the post object to the post detail view controller
        intent.putExtra("selectedPostObject", postObject)

        // Start the activity
        activity.startActivity(intent)
    }
    //*********************************** END GO TO POST DETAIL SEQUENCE ***********************************

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view : View

        // View type 1 is for the notification cell
        return if (viewType == 1) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_cell, parent, false)

            // Return the view holder
            ViewHolderNotificationRow(view)
        } // View type 2 is for the load more button
        else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.hbt_gram_post_item_load_more, parent, false)

            // Return the view holder
            ViewHolderHBTGramLoadMoreNotifications(view)
        }
    }

    override fun getItemCount(): Int {
        // Return number of notifications + 1 (load more button)
        return arrayOfNotifications.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // From first row to number of elements in the array of notifications, show the notification
        if (position in 0 until arrayOfNotifications.size) {
            // In order to prevent us from encountering the class cast exception, we need to do the following
            // Create the GSON object
            val gs = Gson()

            // Convert the notification object which is currently a linked tree map into a JSON string
            val js = gs.toJson(arrayOfNotifications[position])

            // Convert the JSON string back into Notification class
            val notificationModel = gs.fromJson<Notification>(js, Notification::class.java)

            // Call the function to set up the notification row
            (holder as ViewHolderNotificationRow).setUpNotificationRow(notificationModel)
        } // Last row will show the load more row
        else {
            // Call the function to set up the load more row
            (holder as ViewHolderHBTGramLoadMoreNotifications).setUpLoadMoreNotificationRow()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            in 0 until arrayOfNotifications.size -> {
                // From first row to number of elements in the array of notifications, show the notification
                1
            }
            else -> {
                // Last row will show the load more button
                2
            }
        }
    }
}