package com.beta.cuckoo.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.FollowRepository
import com.beta.cuckoo.View.Profile.ProfileDetail
import com.bumptech.glide.Glide
import com.google.gson.Gson

class RecyclerViewAdapterSearchUser (users: ArrayList<User>, activity: Activity, followRepository: FollowRepository) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of users to show
    private val users = users

    // Activity of the parent activity
    private val activity = activity

    // Follow repository
    private val followRepository = followRepository

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
            getFollowStatusBetween2Users(user.getId(), followStatus)
        }
    }
    //****************************************** END VIEW HOLDERS ******************************************

    //****************************************** CHECK FOLLOW STATUS ******************************************
    // The function to get follow status between the currently logged in user and user at this row
    fun getFollowStatusBetween2Users (otherUserId: String, followStatusTextView: TextView) {
        // Call the function to get follow status between the 2 users
        followRepository.checkFollowStatus(otherUserId) {buttonContent ->
            // If the follow status is "yes" (button content is unfollow), set the follow status text view to be following
            if (buttonContent == "Unfollow") {
                followStatusTextView.text = "Following"
            } // Otherwise, set it to be not following
            else {
                followStatusTextView.text = "Not Following"
            }
        }
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