package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.Messages.GetChatMessagePhotoService
import com.beta.myhbt_api.Controller.User.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.Message
import com.beta.myhbt_api.R
import com.bumptech.glide.Glide
import com.google.gson.Gson
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
            // Call the function to load info of the message receiver at this row
            getUserInfoBasedOnId(message.getSender(), senderAvatar, senderFullName)

            // Load message content into the TextView
            messageContent.text = message.getContent()
        }
    }

    // ViewHolder for the chat message with photo
    inner class ViewHolderChatMessageWithPhoto internal  constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView)  {
        // Components from the layout
        private val senderAvatar : ImageView = itemView.findViewById(R.id.senderAvatarMessageWithPhotoCell)
        private val senderFullName : TextView = itemView.findViewById(R.id.senderFullNameMessageWithPhotoCell)
        private val messagePhoto : ImageView = itemView.findViewById(R.id.messagePhoto)

        // The function to set up the message cell with photo
        fun setUpMessageCellWithPhoto (message: Message) {
            // Call the function to load info of the message receiver at this row
            getUserInfoBasedOnId(message.getSender(), senderAvatar, senderFullName)

            // Call the function to load photo of the message into the image view
            getMessagePhoto(message.getMessageId(), messagePhoto)
        }
    }

    // The function to get user info based on id
    private fun getUserInfoBasedOnId (userId: String, senderAvatarImageView: ImageView, senderFullNameTextView: TextView) {
        // Create the get user info base on id service
        val getUserInfoBasedOnUserIdService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetUserInfoBasedOnIdService::class.java)

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

                    // Get full name of the user
                    val fullName = userInfo["fullName"] as String

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
    }

    // The function to load photo of the message based on message id
    private fun getMessagePhoto (messageId: String, messagePhotoImageView: ImageView) {
        // Create the get message photo based on message id service
        val getMessagePhotoBasedOnMessageIdService : GetChatMessagePhotoService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetChatMessagePhotoService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getMessagePhotoBasedOnMessageIdService.getMessagePhoto(messageId)

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

                    // Get image info from the data
                    val imageInfo = (data["documents"] as List<Map<String, Any>>)[0]

                    // Get image URL of the message photo
                    val imageURL = imageInfo["imageURL"] as String

                    // Load image into the ImageView
                    Glide.with(activity)
                        .load(imageURL)
                        .into(messagePhotoImageView)
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view : View

        // Based on view type to return the right view holder
        return if (viewType == 0) {
            // View type 0 is for the message without photo
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_message_cell, parent, false)

            // Return the view holder
            ViewHolderChatMessage(view)
        } else {
            // View type 1 is for the message with photo
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.chat_message_with_photo_cell, parent, false)

            // Return the view holder
            ViewHolderChatMessageWithPhoto(view)
        }
    }

    override fun getItemCount(): Int {
        // Return the number of messages
        return chatMessages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // Convert the users[position] object which is currently a linked tree map into a JSON string
        val js = gs.toJson(chatMessages[position])

        // Convert the JSOn string back into Message class
        val messageModel = gs.fromJson<Message>(js, Message::class.java)

        if (messageModel.getContent() != "image") {
            // If content of message at this row is not "image", call the function to set up the message row without photo
            (holder as ViewHolderChatMessage).setUpMessageCell(messageModel)
        } else {
            // Otherwise, call the function to set up message row with photo
            (holder as ViewHolderChatMessageWithPhoto).setUpMessageCellWithPhoto(messageModel)
        }
    }

    override fun getItemViewType(position: Int): Int {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // Convert the users[position] object which is currently a linked tree map into a JSON string
        val js = gs.toJson(chatMessages[position])

        // Convert the JSOn string back into Message class
        val messageModel = gs.fromJson<Message>(js, Message::class.java)

        return if (messageModel.getContent() != "image") {
            // If content of message at this row is not "image", let it be the message cell without photo
            0
        } else {
            // Otherwise, let it be the message cell with photo
            1
        }
    }
}