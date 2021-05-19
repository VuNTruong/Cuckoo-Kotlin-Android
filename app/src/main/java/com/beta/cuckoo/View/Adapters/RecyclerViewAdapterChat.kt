package com.beta.cuckoo.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.cuckoo.Network.Messages.GetChatMessagePhotoService
import com.beta.cuckoo.Network.User.GetUserInfoBasedOnIdService
import com.beta.cuckoo.Network.RetrofitClientInstance
import com.beta.cuckoo.Model.Message
import com.beta.cuckoo.Model.MessagePhoto
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.MessageRepositories.MessageRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.ZoomImage
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterChat (chatMessages: ArrayList<Message>, activity: Activity, messageRepository: MessageRepository, userRepository: UserRepository) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of chat messages
    private val chatMessages = chatMessages

    // Activity of the parent activity
    private val activity = activity

    // Message repository
    private val messageRepository = messageRepository

    // User repository
    private val userRepository = userRepository

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
            getMessagePhoto(message, messagePhoto)
        }
    }

    // The function to get user info based on id
    private fun getUserInfoBasedOnId (userId: String, senderAvatarImageView: ImageView, senderFullNameTextView: TextView) {
        // Call the function to get info of the currently logged in user based in id
        userRepository.getUserInfoBasedOnId(userId) {userObject ->
            // Load avatar into the ImageView
            Glide.with(activity)
                .load(userObject.getAvatarURL())
                .into(senderAvatarImageView)

            // Load full name into the TextView
            senderFullNameTextView.text = userObject.getFullName()
        }
    }

    // The function to load photo of the message based on message id
    private fun getMessagePhoto (messageObject: Message, messagePhotoImageView: ImageView) {
        // Call the function to get message photo of the message based on message id
        messageRepository.getMessageImageBasedOnId(messageObject.getMessageId()) {messagePhotoObject ->
            // Load image into the ImageView
            Glide.with(activity)
                .load(messagePhotoObject.getImageURL())
                .into(messagePhotoImageView)

            // Set on click listener for the image view so that it will take user to the
            // zoom photo activity
            messagePhotoImageView.setOnClickListener {
                // Go to zoom activity
                gotoZoom(messagePhotoObject)
            }
        }
    }

    //*********************************** ADDITIONAL FUNCTIONS ***********************************
    // The function which will take user to the activity where user can zoom in and out an image
    private fun gotoZoom (imageObject: MessagePhoto) {
        if (imageObject.getImageURL() == "") {
            return
        }

        // The intent object
        val intent = Intent(activity, ZoomImage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Let the activity know which image to load
        intent.putExtra("imageURLToLoad", imageObject.getImageURL())

        // Let the zoom activity know that image comes from message
        intent.putExtra("imageComesFromMessage", true)

        // Let the zoom activity know who is creator of the image
        intent.putExtra("imageURLToLoad", imageObject.getImageURL())

        // Let the zoom activity know message id of the message to which the image belongs to
        intent.putExtra("messageId", imageObject.getMessageID())

        // Start the activity
        activity.startActivity(intent)
    }
    //*********************************** ADDITIONAL FUNCTIONS ***********************************

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