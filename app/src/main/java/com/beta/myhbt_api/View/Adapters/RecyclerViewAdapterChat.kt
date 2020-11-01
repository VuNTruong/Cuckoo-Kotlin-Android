package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.Message
import com.beta.myhbt_api.R
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterChat (chatMessages: ArrayList<Message>, activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of chat messages
    private val chatMessages = chatMessages

    // Activity of the parent activity
    private val activity = activity

    // ViewHolder for the chat message
    inner class ViewHolderChatMessage internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val senderAvatar : ImageView = itemView.findViewById(R.id.senderAvatarMessageCell)
        private val senderFullName : TextView = itemView.findViewById(R.id.senderFullNameMessageCell)
        private val messageContent : TextView = itemView.findViewById(R.id.messageContentMessageCell)

        // The function to set up the message cell
        fun setUpMessageCell (message: Message) {
            // Execute the AsyncTask to get info of the sender
            GetUserInfoBasedOnUserId().execute(hashMapOf(
                "userId" to message.getSender(),
                "senderAvatarImageView" to senderAvatar,
                "senderFullNameTextView" to senderFullName
            ))

            // Load message content into the TextView
            messageContent.text = message.getContent()
        }
    }

    // AsyncTask to get user info based on user id
    inner class GetUserInfoBasedOnUserId : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get user id of the user
            val userId = params[0]!!["userId"] as String

            // The sender avatar image view
            val senderAvatarImageView = params[0]!!["senderAvatarImageView"] as ImageView

            // The sender full name text view
            val senderFullNameTextView = params[0]!!["senderFullNameTextView"] as TextView

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
                            .into(senderAvatarImageView)

                        // Load full name into the TextView
                        senderFullNameTextView.text = fullName
                    }
                }
            })

            return null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Return the view holder
        return ViewHolderChatMessage(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_message_cell, parent, false))
    }

    override fun getItemCount(): Int {
        // Return the number of messages
        return chatMessages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Call the function to set up the message row
        (holder as ViewHolderChatMessage).setUpMessageCell(chatMessages[position])
    }
}