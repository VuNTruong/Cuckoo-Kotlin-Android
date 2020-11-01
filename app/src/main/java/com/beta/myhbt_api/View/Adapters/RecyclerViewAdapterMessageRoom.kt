package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.beta.myhbt_api.Controller.GetLatestMessageOfMessageRoomService
import com.beta.myhbt_api.Controller.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.MessageRoom
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Chat
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterMessageRoom (currentUserId: String, messageRoom : ArrayList<MessageRoom>, activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // User id of the currently logged in user
    private val currentUserId = currentUserId

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
            if (messageRoom.getUser1() == currentUserId) {
                // Execute the AsyncTask to get info of user2
                GetUserInfoBasedOnUserId().execute(hashMapOf(
                    "userId" to messageRoom.getUser2(),
                    "userAvatarImageView" to userAvatar,
                    "userFullNameTextView" to userFullName
                ))
            } // Otherwise, based on id of user1 to get full name and avatar
            else {
                // Execute the ASyncTask to get info of user1
                GetUserInfoBasedOnUserId().execute(hashMapOf(
                    "userId" to messageRoom.getUser1(),
                    "userAvatarImageView" to userAvatar,
                    "userFullNameTextView" to userFullName
                ))
            }

            // Execute the AsyncTask to get latest message of the message room
            GetLatestMessageOfMessageRoom().execute(hashMapOf(
                "latestMessageContentTextView" to latestMessageContent,
                "messageRoomId" to messageRoom.getMessageRoomId(),
                "userAvatarImageView" to userAvatar,
                "userFullNameTextView" to userFullName
            ))
        }
    }

    // AsyncTask to get user info based on user id
    inner class GetUserInfoBasedOnUserId : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get user id of the user
            val userId = params[0]!!["userId"] as String

            // The sender avatar image view
            val userAvatarImageView = params[0]!!["userAvatarImageView"] as ImageView

            // The sender full name text view
            val userFullNameTextView = params[0]!!["userFullNameTextView"] as TextView

            // Create the get user info base on id service
            val getUserInfoBasedOnUserIdService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(GetUserInfoBasedOnIdService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getUserInfoBasedOnUserIdService.getUserInfoBasedOnId(userId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that there is no error
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get user info from the data
                        val userInfo = (data["documents"] as List<Map<String, Any>>)[0]

                        // Get name of the sender
                        val firstName = userInfo["firstName"] as String
                        val middleName = userInfo["middleName"] as String
                        val lastName = userInfo["lastName"] as String
                        // Combine them all to get the full name
                        val fullName = "$lastName $middleName $firstName"

                        // Get avatar URL of the sender
                        val senderAvatarURL = userInfo["avatarURL"] as String

                        // Load avatar into the ImageView
                        Glide.with(activity)
                            .load(senderAvatarURL)
                            .into(userAvatarImageView)

                        // Load full name into the TextView
                        userFullNameTextView.text = fullName
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to get latest message of the specified message room
    inner class GetLatestMessageOfMessageRoom : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get message content TextView
            val latestMessageContentTextView = params[0]!!["latestMessageContentTextView"] as TextView

            // Get message room id
            val messageRoomId = params[0]!!["messageRoomId"] as String

            // Create the get latest message of message room service
            val getLatestMessageOfMessageRoomService: GetLatestMessageOfMessageRoomService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(GetLatestMessageOfMessageRoomService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getLatestMessageOfMessageRoomService.getLatestMessageOfMessageRoom(messageRoomId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that there is no error
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get content of the latest message
                        val latestMessageContent = data["content"] as String

                        // Get sender of the latest message
                        val latestMessageSender = data["sender"] as String

                        // Check to see if latest message is written by the current user or not
                        if (latestMessageSender == currentUserId) {
                            // Load content of the latest message into the TextView
                            // Also let the user know that it is sent by the current user
                            latestMessageContentTextView.text = "You: $latestMessageContent"
                        } // Otherwise, just load the content in
                        else {
                            latestMessageContentTextView.text = latestMessageContent
                        }
                    }
                }
            })

            return null
        }
    }

    // The function to take user to the activity where the user can chat with the selected user
    private fun gotoChat (messageRoom: MessageRoom) {
        // If user1 of the message room is the currently logged in user, user id of user2 will be message receiver id
        val receiverUserId = if (messageRoom.getUser1() == currentUserId) {
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