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
import com.beta.cuckoo.Repository.MessageRepositories.MessageRepository
import com.beta.cuckoo.View.Chat.Chat
import com.bumptech.glide.Glide
import com.google.gson.Gson

class RecyclerViewAdapterSearchUserToChatWith (users: ArrayList<User>, activity: Activity, messageRepository: MessageRepository) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of users to show
    private val users = users

    // Activity of the parent activity
    private val activity = activity

    // Message repository
    private val messageRepository = messageRepository

    // ViewHolder for the user show cell
    inner class ViewHolderSearchCell internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val userAvatar : ImageView = itemView.findViewById(R.id.userAvatarSearchUserCell)
        private val userFullName : TextView = itemView.findViewById(R.id.userFullNameSearchUserCell)
        private val mView = itemView

        // The function to set up user info row for the search list
        fun setUpSearchRow (user: User) {
            // Set up on click listener for the view so that it will take user to the activity where
            // the user can chat with the selected user
            mView.setOnClickListener {
                // Call the function to check if there is message between the 2 users or not
                checkChatRoomAndGotoChatRoom(user.getId())
            }

            // Load full name into the TextView for the user
            userFullName.text = user.getFullName()

            // Load avatar of the user into the ImageView
            Glide.with(activity)
                .load(user.getAvatarURL())
                .into(userAvatar)
        }
    }

    // The function to check for chat room between the 2 users and go to chat room
    fun checkChatRoomAndGotoChatRoom (messageReceiverUserId: String) {
        // Call the function to check for chat room between the 2 users
        messageRepository.checkChatRoomBetween2Users(messageReceiverUserId) {chatRoomId ->
             // Create the intent which will take user to the activity where the user can chat with the selected user
            val intent = Intent(activity, Chat::class.java)

            // Put user id of the message receiver and chat room id into the intent so that next activity
            // will know which user to send message to and which message room to work with
            intent.putExtra("chatRoomId", chatRoomId).putExtra("receiverUserId", messageReceiverUserId)

            // Start the activity
            activity.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_user_cell, parent, false)

        // Return the ViewHolder
        return ViewHolderSearchCell(view)
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
        (holder as ViewHolderSearchCell).setUpSearchRow(userModel)
    }
}