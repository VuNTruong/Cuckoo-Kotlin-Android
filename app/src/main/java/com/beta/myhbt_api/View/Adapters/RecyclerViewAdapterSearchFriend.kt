package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetFollowStatusService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.ProfileDetail
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterSearchFriend (users: ArrayList<User>, activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of users to show
    private val users = users

    // Activity of the parent activity
    private val activity = activity

    //****************************************** VIEW HOLDERS ******************************************
    // ViewHolder for the user show cell
    inner class ViewHolderSearchFriendCell internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val userAvatar : ImageView = itemView.findViewById(R.id.userAvatarSearchFriendCell)
        private val userFullName : TextView = itemView.findViewById(R.id.userFullNameSearchFriendCell)
        private val followStatus : TextView = itemView.findViewById(R.id.followStatusSearchFriendCell)
        private val mView = itemView

        // The function to set up the search friend cell
        fun setUpSearchFriendCell (user: User) {
            // Set on click listener for the view so that it will take user to the activity where the user can see profile detail of the selected user
            mView.setOnClickListener {
                // Take user to the activity where the user can view profile detail of the selected user
                val intent = Intent(activity, ProfileDetail::class.java)

                // Pass the selected user object to the next activity as well
                intent.putExtra("selectedUserObject", user)

                // Start the activity
                activity.startActivity(intent)
            }

            // Load full name of the user into the TextView
            userFullName.text = user.getFullName()

            // Load avatar of the user into the ImageView
            Glide.with(activity)
                .load(user.getAvatarURL())
                .into(userAvatar)

            // Call the function to get follow status between the 2 users
            getInfoOfCurrentUserAndCheckFollowStatus(user.getId(), followStatus)
        }
    }
    //****************************************** END VIEW HOLDERS ******************************************

    //****************************************** CHECK FOLLOW STATUS ******************************************
    // The function to get info of the current user and check to see if current user follow the user at this activity or not
    fun getInfoOfCurrentUserAndCheckFollowStatus (otherUserId: String, followStatusTextView: TextView) {
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

                    // Get user id of the currently logged in user
                    val userId = data["_id"] as String

                    // Call the function to get follow status between the 2 users
                    getFollowStatus(userId, otherUserId, followStatusTextView)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to get follow status between the 2 users
    fun getFollowStatus (currentUserId: String, otherUserId: String, followStatusTextView: TextView) {
        // Get follow status service
        val getFollowStatusService: GetFollowStatusService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetFollowStatusService::class.java)

        // Create the call object to perform the call
        val call: Call<Any> = getFollowStatusService.getFollowStatus(currentUserId, otherUserId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("There seem to be an error ${t.stackTrace}")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data of the response (follow status between the 2 users)
                    val data = responseBody["data"] as String

                    // If the follow status is "yes", set the follow status text view to be following
                    if (data == "Yes") {
                        followStatusTextView.text = "Following"
                    } // Otherwise, set it to be not following
                    else {
                        followStatusTextView.text = "Not Following"
                    }
                } else {
                    print("Something is not right")
                }
            }

        })
    }
    //****************************************** END CHECK FOLLOW STATUS ******************************************

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_friend_cell, parent, false)

        // Return the ViewHolder
        return ViewHolderSearchFriendCell(view)
    }

    override fun getItemCount(): Int {
        // Return number of users
        return users.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // Convert the users[position] object which is currently a linked tree map into a JSON string
        val js = gs.toJson(users[position])

        // Convert the JSOn string back into User class
        val userModel = gs.fromJson<User>(js, User::class.java)

        // Call the function to set up the search cell
        (holder as ViewHolderSearchFriendCell).setUpSearchFriendCell(userModel)
    }
}