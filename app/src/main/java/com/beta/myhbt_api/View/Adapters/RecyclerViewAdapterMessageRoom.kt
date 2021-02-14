package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.beta.myhbt_api.Controller.Messages.GetLatestMessageOfMessageRoomService
import com.beta.myhbt_api.Controller.User.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.MessageRoom
import com.beta.myhbt_api.R
import com.beta.myhbt_api.Repository.MessageRepositories.MessageRepository
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.beta.myhbt_api.View.Chat
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService

class RecyclerViewAdapterMessageRoom (messageRoom : ArrayList<MessageRoom>, activity: Activity, executorService: ExecutorService) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // The user repository
    private val userInfoRepository: UserRepository = UserRepository(executorService, activity)

    // The message repository
    private val messageRepository: MessageRepository = MessageRepository(executorService, activity)

    // Array of message room in which the current user is involved
    private val messageRoom = messageRoom

    // Activity of the parent activity
    private val activity = activity

    // ViewHolder for the message room
    inner class ViewHolderMessageRoom internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val userAvatar : ImageView = itemView.findViewById(R.id.userAvatarMessageRoomCell)
        private val userFullName : TextView = itemView.findViewById(R.id.userFullNameMessageRoomCell)
        private val latestMessageContent : TextView = itemView.findViewById(R.id.messageContentMessageRoomCell)
        private val mView = itemView

        // The function to set up the message room cell
        fun setUpMessageRoomCell (messageRoom: MessageRoom) {
            // Set on click listener for the view so that the view will take user to the activity where the user can start chatting
            mView.setOnClickListener {
                // Call the function to take user to the activity where the user can start chatting
                gotoChat(messageRoom)
            }

            // Check to see if user1 is the current user or not
            // If user1 is the current user, based on id of user2 to get full name and avatar
            userInfoRepository.getInfoOfCurrentUser { userObject ->
                if (messageRoom.getUser1() == userObject.getId()) {
                    // Call the function to get info of the user
                    getUserInfoBasedOnId(messageRoom.getUser2(), userAvatar, userFullName)
                } // Otherwise, based on id of user1 to get full name and avatar
                else {
                    // Call the function to get info of the user
                    getUserInfoBasedOnId(messageRoom.getUser1(), userAvatar, userFullName)
                }

                // Call the function to get latest message of the chat room
                getLatestMessageOfMessageRoom(messageRoom.getMessageRoomId(), latestMessageContent)
            }
        }
    }

    // The function to get user info based on user id
    fun getUserInfoBasedOnId (userId: String, userAvatarImageView: ImageView, userFullNameTextView: TextView) {
        // Call the function to get user info based on id
        userInfoRepository.getUserInfoBasedOnId(userId) {userObject ->
            // Load full name into the text view
            userFullNameTextView.text = userObject.getFullName()

            // Load avatar into the image view
            Glide.with(activity)
                .load(userObject.getAvatarURL())
                .into(userAvatarImageView)
        }
    }

    // The function to get latest message of the message room
    fun getLatestMessageOfMessageRoom (messageRoomId: String, latestMessageContentTextView: TextView) {
        // Call the function to get latest message in the message room
        messageRepository.getLatestMessageInMessageRoom(messageRoomId) {latestMessageContent ->
            // Load latest message content into the text view
            latestMessageContentTextView.text = latestMessageContent
        }
    }

    // The function to take user to the activity where the user can chat with the selected user
    private fun gotoChat (messageRoom: MessageRoom) {
        userInfoRepository.getInfoOfCurrentUser { userObject ->
            // If user1 of the message room is the currently logged in user, user id of user2 will be message receiver id
            val receiverUserId = if (messageRoom.getUser1() == userObject.getId()) {
                messageRoom.getUser2()
            } // Otherwise, user id of user1 will be the message receiver id
            else {
                messageRoom.getUser1()
            }

            // Get the message room id
            val chatRoomId = messageRoom.getMessageRoomId()

            // Go to the activity where the user can start chatting
            val intent = Intent(activity, Chat::class.java)

            // Put info in the intent so that next activity will know who to chat with
            intent.putExtra("chatRoomId", chatRoomId)
            intent.putExtra("receiverUserId", receiverUserId)
            // Start the activity
            activity.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Return the view holder
        return ViewHolderMessageRoom(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.message_room_cell, parent, false))
    }

    override fun getItemCount(): Int {
        // Return number of message room of the user
        return messageRoom.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Call the function to set up the message room row
        (holder as ViewHolderMessageRoom).setUpMessageRoomCell(messageRoom[position])
    }
}