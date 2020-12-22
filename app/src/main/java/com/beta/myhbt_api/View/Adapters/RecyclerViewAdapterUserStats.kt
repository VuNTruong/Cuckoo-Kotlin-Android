package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.UserCommentInteraction
import com.beta.myhbt_api.Model.UserInteraction
import com.beta.myhbt_api.Model.UserLikeInteraction
import com.beta.myhbt_api.Model.UserProfileVisit
import com.beta.myhbt_api.R
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterUserStats (arrayOfUserInteraction: ArrayList<UserInteraction>, arrayOfUserLikeInteraction: ArrayList<UserLikeInteraction>,
                                    arrayOfUserCommentInteraction: ArrayList<UserCommentInteraction>, arrayOfUserProfileVisit: ArrayList<UserProfileVisit>,
                                    activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of users interact with the current user
    private val arrayOfUserInteraction = arrayOfUserInteraction

    // Array of users like posts by the current user
    private val arrayOFUserLikeInteraction = arrayOfUserLikeInteraction

    // Array of users comment posts by the current user
    private val arrayOfUserCommentInteraction = arrayOfUserCommentInteraction

    // Array of users visit profile of the current user
    private val arrayOfUserProfileVisit = arrayOfUserProfileVisit

    // Activity of the parent
    val activity = activity

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

        // The function to set up the user stats content row
        fun setUpUserStatsContentRow (userId: String, subContent: String) {
            // Load sub-content into the TextView
            userStatsItemSubContent.text = subContent

            // Call the function to load info of user at this row
            getUserInfoBasedOnId(userId, userStatsItemContent, userStatsItemAvatar)
        }
    }

    // ViewHolder for the user stats load more row
    inner class ViewHolderUserStatsLoadMoreRow internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val userStatsLoadMoreview : ConstraintLayout = itemView.findViewById(R.id.userStatsSeeMoreView)

        // The function to set up the user stats see more row
        fun setUpUserStatsSeeMoreRow () {
            // Set up on click listener
            userStatsLoadMoreview.setOnClickListener {

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
            1 -> {
                // View type 1 is for the category item
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.user_stats_item, parent, false)

                // Return the view holder
                ViewHolderUserStatsContentRow(view)
            }
            else -> {
                // view type 2 is for the see more row
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.user_stats_see_more, parent, false)

                // Return the view holder
                ViewHolderUserStatsLoadMoreRow(view)
            }
        }
    }

    override fun getItemCount(): Int {
        /*
        We will have these rows
        1. First category (user interact with the most)
        2. List of users interact with the most
        3. See more users interact with the most
        4. Second category (user like the most)
        5. List of users like the most
        6. See more users like the most
        7. Third category (user comment the most)
        8. List of users comment the most
        9. See more users comment the most
        10. Fourth category (user visit profile the most)
        11. List of users visit profile the most
        12. See more users visit profile the most
         */
        return 1 + arrayOfUserInteraction.size + 1 + 1 + arrayOFUserLikeInteraction.size + 1 +
                1 + arrayOfUserCommentInteraction.size + 1 + 1 + arrayOfUserProfileVisit.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // First row is for the first category
        when (position) {
            0 -> {
                (holder as ViewHolderUserStatsCategory).setUpUserStatsCategoryRow("Users interact with you the most")
            } // From second row, show list of users interact with the most
            in 1 .. arrayOfUserInteraction.size -> {
                // Convert objects of the array of user interaction which is currently a linked tree map into a JSON string
                val jsUserInteraction = gs.toJson(arrayOfUserInteraction[position - 1])

                // Convert the JSOn string back into UserInteraction class
                val userInteractionObject = gs.fromJson<UserInteraction>(jsUserInteraction, UserInteraction::class.java)

                (holder as ViewHolderUserStatsContentRow).setUpUserStatsContentRow(userInteractionObject.getInteractedUser(),
                    "${userInteractionObject.getUserInteractionFrequency()} interactions (include like, comments)")
            } // After that, show the load more row
            arrayOfUserInteraction.size + 1 -> {
                (holder as ViewHolderUserStatsLoadMoreRow).setUpUserStatsSeeMoreRow()
            }

            // After that, show the second category
            arrayOfUserInteraction.size + 2 -> {
                (holder as ViewHolderUserStatsCategory).setUpUserStatsCategoryRow("Users like your posts the most")
            } // After that, show list of users like posts the most
            in arrayOfUserInteraction.size + 3 .. arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + 2 -> {
                // Convert objects of the array of user like interaction which is currently a linked tree map into a JSON string
                val jsUserLikeInteraction = gs.toJson(arrayOFUserLikeInteraction[position - arrayOfUserInteraction.size - 3])

                // Convert the JSOn string back into UserLikeInteraction class
                val userLikeInteractionObject = gs.fromJson<UserLikeInteraction>(jsUserLikeInteraction, UserLikeInteraction::class.java)

                (holder as ViewHolderUserStatsContentRow).setUpUserStatsContentRow(userLikeInteractionObject.getUserLiked(),
                    "${userLikeInteractionObject.getUserNumOfLikes()} likes")
            }// After that, show the load more row
            arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + 3 -> {
                (holder as ViewHolderUserStatsLoadMoreRow).setUpUserStatsSeeMoreRow()
            }

            // After that, show the third category
            arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + 4 -> {
                (holder as ViewHolderUserStatsCategory).setUpUserStatsCategoryRow("Users comment you posts the most")
            } // After that, show list of users comment posts the most
            in arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + 5 ..
                    arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + 4 -> {
                // Convert objects of the array of user comment interaction which is currently a linked tree map into a JSON string
                val jsUserCommentInteraction = gs.toJson(arrayOfUserCommentInteraction[position - arrayOfUserInteraction.size - arrayOFUserLikeInteraction.size - 5])

                // Convert the JSOn string back into UserCommentInteraction class
                val userCommentInteractionObject = gs.fromJson<UserCommentInteraction>(jsUserCommentInteraction, UserCommentInteraction::class.java)

                (holder as ViewHolderUserStatsContentRow).setUpUserStatsContentRow(userCommentInteractionObject.getUserCommented(),
                    "${userCommentInteractionObject.getUserNumOfComments()} comments")
            } // After that, show the load more row
            arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + 5 -> {
                (holder as ViewHolderUserStatsLoadMoreRow).setUpUserStatsSeeMoreRow()
            }

            // After that, show the fourth category
            arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + 6 -> {
                (holder as ViewHolderUserStatsCategory).setUpUserStatsCategoryRow("Users visit your profile the most")
            } // After that, show list of users visit profile the most
            in arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + 7 ..
                    arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + arrayOfUserProfileVisit.size + 6 -> {
                // Convert objects of the array of user profile visit which is currently a linked tree map into a JSON string
                val jsUserProfileVisit = gs.toJson(arrayOfUserProfileVisit[position - arrayOfUserInteraction.size - arrayOFUserLikeInteraction.size - arrayOfUserCommentInteraction.size - 7])

                // Convert the JSOn string back into UserProfileVisit class
                val userProfileVisitObject = gs.fromJson<UserProfileVisit>(jsUserProfileVisit, UserProfileVisit::class.java)

                (holder as ViewHolderUserStatsContentRow).setUpUserStatsContentRow(userProfileVisitObject.getUserVisited(),
                    "${userProfileVisitObject.getUserNumOfVisits()} visits")
            } // After that, show the load more row
            arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + arrayOfUserProfileVisit.size + 7 -> {
                (holder as ViewHolderUserStatsLoadMoreRow).setUpUserStatsSeeMoreRow()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                0
            } // From second row, show list of users interact with the most
            in 1 .. arrayOfUserInteraction.size -> {
                1
            } // After that, show the load more row
            arrayOfUserInteraction.size + 1 -> {
                2
            }

            // After that, show the second category
            arrayOfUserInteraction.size + 2 -> {
                0
            } // After that, show list of users like posts the most
            in arrayOfUserInteraction.size + 3 .. arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + 2 -> {
                1
            }// After that, show the load more row
            arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + 3 -> {
                2
            }

            // After that, show the third category
            arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + 4 -> {
                0
            } // After that, show list of users comment posts the most
            in arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + 5 ..
                    arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + 4 -> {
                1
            } // After that, show the load more row
            arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + 5 -> {
                2
            }

            // After that, show the fourth category
            arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + 6 -> {
                0
            } // After that, show list of users visit profile the most
            in arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + 7 ..
                    arrayOfUserInteraction.size + arrayOFUserLikeInteraction.size + arrayOfUserCommentInteraction.size + arrayOfUserProfileVisit.size + 6 -> {
                1
            } // After that, show the load more row
            else -> {
                2
            }
        }
    }
}