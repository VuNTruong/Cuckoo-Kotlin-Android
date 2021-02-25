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
import com.beta.cuckoo.View.UserInfoView.ProfileDetail
import com.bumptech.glide.Glide
import com.google.gson.Gson

class RecyclerViewAdapterUserShow (users: ArrayList<User>, activity: Activity): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of users to show
    private val users = users

    // Activity of the parent activity
    private val activity = activity

    //******************************** VIEW HOLDERS FOR THE RECYCLER VIEW ********************************
    // ViewHolder for the user show cell
    inner class ViewHolderUserShowCell internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val userAvatar : ImageView = itemView.findViewById(R.id.userAvatarSearchUserCell)
        private val userFullName : TextView = itemView.findViewById(R.id.userFullNameSearchUserCell)
        private val mView = itemView

        // The function to set up the user show cell
        fun setUpUserShowCell (user: User) {
            // Set on click listener for the view so that it will take user to the activity
            // where the user can see profile detail of the user at this row
            mView.setOnClickListener {
                // Call the function to go to profile detail
                gotoProfileDetail(user)
            }

            // Load full name of the user into the TextView
            userFullName.text = user.getFullName()

            // Load avatar of the user into the ImageView
            Glide.with(activity)
                .load(user.getAvatarURL())
                .into(userAvatar)
        }
    }
    //******************************** END VIEW HOLDERS FOR THE RECYCLER VIEW ********************************

    //******************************** START PROFILE DETAIL ACTIVITY SEQUENCE ********************************
    // The function to take user to the activity where the user can see profile detail of the selected user
    fun gotoProfileDetail (user: User) {
        // Take user to the activity where the user can view profile detail of the selected user
        val intent = Intent(activity, ProfileDetail::class.java)

        // Pass the selected user object to the next activity as well
        intent.putExtra("selectedUserObject", user)

        // Start the activity
        activity.startActivity(intent)
    }
    //******************************** END START PROFILE DETAIL ACTIVITY SEQUENCE ********************************

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_user_cell, parent, false)

        // Return the ViewHolder
        return ViewHolderUserShowCell(view)
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

        // Call the function to set up the user show cell
        (holder as ViewHolderUserShowCell).setUpUserShowCell(userModel)
    }
}