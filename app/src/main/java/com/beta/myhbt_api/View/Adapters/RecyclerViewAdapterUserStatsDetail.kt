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
import com.beta.myhbt_api.Controller.User.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.*
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.ProfileDetail
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterUserStatsDetail (arrayOfUserInteraction: ArrayList<UserInteraction>, arrayOfUserLikeInteraction: ArrayList<UserLikeInteraction>,
                                        arrayOfUserCommentInteraction: ArrayList<UserCommentInteraction>, arrayOfUserProfileVisit: ArrayList<UserProfileVisit>,
                                        activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of user interaction
    private val arrayOfUserInteraction = arrayOfUserInteraction

    // Array of user like interaction
    private val arrayOfUserLikeInteraction = arrayOfUserLikeInteraction

    // Array of user comment interaction
    private val arrayOfUserCommentInteraction = arrayOfUserCommentInteraction

    // Array of user profile visit
    private val arrayOfUserProfileVisit = arrayOfUserProfileVisit

    // Activity of the parent
    private val activity = activity

    // ViewHolder for user stats category
    inner class ViewHolderUserStatsCategory internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val userStatsCategory : TextView = itemView.findViewById(R.id.userStatsCategoryTitle)

        // The function to set up the user stats category row
        fun setUpUserStatsCategoryRow (categoryTitle: String) {
            // Load category into the TextView
            userStatsCategory.text = categoryTitle
        }
    }

    // ViewHolder for the user stats content row
    inner class ViewHolderUserStatsContentRow internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val userStatsItemAvatar : ImageView = itemView.findViewById(R.id.userStatsItemAvatar)
        private val userStatsItemContent : TextView = itemView.findViewById(R.id.userStatsItemContent)
        private val userStatsItemSubContent : TextView = itemView.findViewById(R.id.userStatsItemSubContent)
        private var userStatsView : ConstraintLayout = itemView.findViewById(R.id.userStatsView)

        // The function to set up the user stats content row
        fun setUpUserStatsContentRow (userId: String, subContent: String) {
            // Load sub-content into the TextView
            userStatsItemSubContent.text = subContent

            // Call the function to load info of user at this row
            getUserInfoBasedOnId(userId, userStatsItemContent, userStatsItemAvatar)

            // Set on click listener for the user stats view (take user to the activity where the user can see profile detail of the user)
            userStatsView.setOnClickListener {
                // Call the function to get user info based on id and take user to the activity where the
                // user can see profile detail of that user
                getUserInfoBasedOnIdAndGotoProfileDetail(userId)
            }
        }
    }

    //******************************** LOAD INFO OF USER BASED ON ID ********************************
    // The function to get user info based on id
    fun getUserInfoBasedOnId(userId: String, userFullNameTextView: TextView, userAvatarImageView: ImageView) {
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
                    val body = response.body()
                    print(body)
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    // Get obtain user data from that
                    val data = (((responseBody["data"] as Map<String, Any>)["documents"]) as List<Map<String, Any>>)[0]

                    // Load full name of the user
                    val userFullName = data["fullName"] as String

                    // Load avatar URL of the user
                    val avatarURL = data["avatarURL"] as String

                    // Load full name into the TextView
                    userFullNameTextView.text = userFullName

                    // Load user avatar into the ImageView
                    Glide.with(activity)
                        .load(avatarURL)
                        .into(userAvatarImageView)
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //******************************** END LOAD INFO OF USER BASED ON ID ********************************

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

        // Based on view type to return the right view holder
        return when (viewType) {
            0 -> {
                // View type 0 is for the category
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.user_stats_category, parent, false)

                // Return the view holder
                ViewHolderUserStatsCategory(view)
            }
            else -> {
                // View type 1 is for the category item
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.user_stats_item, parent, false)

                // Return the view holder
                ViewHolderUserStatsContentRow(view)
            }
        }
    }

    override fun getItemCount(): Int {
        // Return number of items in array of user interaction objects + 1 (header)
        // Only 1 array will have items in it
        return arrayOfUserInteraction.size + arrayOfUserLikeInteraction.size + arrayOfUserCommentInteraction.size + arrayOfUserProfileVisit.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // Based on which array of not blank to do the right thing
        // only need to show info of array which is not blank
        if (arrayOfUserInteraction.size != 0) {
            when (position) {
                // First row is the header
                0 -> {
                    // Call the function to set up the header
                    (holder as ViewHolderUserStatsCategory).setUpUserStatsCategoryRow("Who interact with you")
                } // The rest is for the content row
                else -> {
                    // Convert objects of the array of user interaction which is currently a linked tree map into a JSON string
                    val jsUserInteraction = gs.toJson(arrayOfUserInteraction[position - 1])

                    // Convert the JSOn string back into UserInteraction class
                    val userInteractionObject = gs.fromJson<UserInteraction>(jsUserInteraction, UserInteraction::class.java)

                    // Call the function to set up the content row
                    (holder as ViewHolderUserStatsContentRow).setUpUserStatsContentRow(userInteractionObject.getInteractedUser(),
                        "${userInteractionObject.getUserInteractionFrequency()}")
                }
            }
        }

        if (arrayOfUserLikeInteraction.size != 0) {
            when (position) {
                // First row is the header
                0 -> {
                    // Call the function to set up the header
                    (holder as ViewHolderUserStatsCategory).setUpUserStatsCategoryRow("Who like your posts")
                } // The rest is for the content row
                else -> {
                    // Convert objects of the array of user like interaction which is currently a linked tree map into a JSON string
                    val jsUserLikeInteraction = gs.toJson(arrayOfUserLikeInteraction[position - 1])

                    // Convert the JSOn string back into UserLikeInteraction class
                    val userLikeInteractionObject = gs.fromJson<UserLikeInteraction>(jsUserLikeInteraction, UserLikeInteraction::class.java)

                    // Call the function to set up the content row
                    (holder as ViewHolderUserStatsContentRow).setUpUserStatsContentRow(userLikeInteractionObject.getUserLiked(),
                        "${userLikeInteractionObject.getUserNumOfLikes()}")
                }
            }
        }

        if (arrayOfUserCommentInteraction.size != 0) {
            when (position) {
                // First row is the header
                0 -> {
                    // Call the function to set up the header
                    (holder as ViewHolderUserStatsCategory).setUpUserStatsCategoryRow("Who comment your posts")
                } // The rest is for the content row
                else -> {
                    // Convert objects of the array of user comment interaction which is currently a linked tree map into a JSON string
                    val jsUserCommentInteraction = gs.toJson(arrayOfUserCommentInteraction[position - arrayOfUserInteraction.size - 1])

                    // Convert the JSOn string back into UserCommentInteraction class
                    val userCommentInteractionObject = gs.fromJson<UserCommentInteraction>(jsUserCommentInteraction, UserCommentInteraction::class.java)

                    (holder as ViewHolderUserStatsContentRow).setUpUserStatsContentRow(userCommentInteractionObject.getUserCommented(),
                        "${userCommentInteractionObject.getUserNumOfComments()} comments")
                }
            }
        }

        if (arrayOfUserProfileVisit.size != 0) {
            when (position) {
                // First row is the header
                0 -> {
                    // Call the function to set up the header
                    (holder as ViewHolderUserStatsCategory).setUpUserStatsCategoryRow("Who comment your posts")
                } // The rest is for the content row
                else -> {
                    // Convert objects of the array of user profile visit which is currently a linked tree map into a JSON string
                    val jsUserProfileVisit = gs.toJson(arrayOfUserProfileVisit[position - 1])

                    // Convert the JSOn string back into UserProfileVisit class
                    val userProfileVisitObject = gs.fromJson<UserProfileVisit>(jsUserProfileVisit, UserProfileVisit::class.java)

                    (holder as ViewHolderUserStatsContentRow).setUpUserStatsContentRow(userProfileVisitObject.getUserVisited(),
                        "${userProfileVisitObject.getUserNumOfVisits()} visits")
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            // First row is for the header
            0 -> {
                0
            } // The rest is for the content row
            else -> {
                1
            }
        }
    }
}