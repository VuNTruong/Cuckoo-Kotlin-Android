package com.beta.cuckoo.View.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.UserBlockRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.AudioChat.AudioChat
import com.beta.cuckoo.View.UserInfoView.ProfileDetail
import com.beta.cuckoo.View.VideoChat.VideoChat
import com.beta.cuckoo.View.ZoomImage
import com.bumptech.glide.Glide

class RecyclerViewAdapterMessageOption (messageReceiverUserId: String, chatRoomId: String, context: Context, userRepository: UserRepository, userBlockRepository: UserBlockRepository,
                                        arrayOfMessagePhotos: ArrayList<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // User id of the message receiver
    private val messageReceiverUserId = messageReceiverUserId

    // Chat room id in which the 2 users are in
    private val chatRoomId = chatRoomId

    // Array of message photo
    private val arrayOfMessagePhotos = arrayOfMessagePhotos

    // Context of parent activity
    private val context = context

    // User repository
    private val userRepository = userRepository

    // User block repository
    private val userBlockRepository = userBlockRepository

    //*********************************** VIEW HOLDERS FOR THE RECYCLER VIEW ***********************************
    // ViewHolder for the message option menu header
    inner class ViewHolderMessageOptionMenuHeader internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val messageReceiverAvatar : ImageView = itemView.findViewById(R.id.messageReceiverAvatarMessageOption)
        private val messageReceiverFullName : TextView = itemView.findViewById(R.id.messageReceiverFullNameMessageOption)
        private val profileButton : ConstraintLayout = itemView.findViewById(R.id.profileButtonMessageOptions)
        private val videoCallButton : ConstraintLayout = itemView.findViewById(R.id.videoCallButtonMessageOption)
        private val audioCallButton : ConstraintLayout = itemView.findViewById(R.id.audioCallButtonMessageOption)

        // The function to set up message option header row
        fun setUpHeaderRow (userId: String) {
            // Call the function to load info of user based on user id
            userRepository.getUserInfoBasedOnId(userId) {userObject ->
                // Load user avatar into the image view
                Glide.with(context)
                    .load(userObject.getAvatarURL())
                    .into(messageReceiverAvatar)

                // Load user full name into the TextView
                messageReceiverFullName.text = userObject.getFullName()
            }

            // Set up on click listener for the profile button
            profileButton.setOnClickListener {
                // Call the function to get user object of user with specified user id
                userRepository.getUserInfoBasedOnId(messageReceiverUserId) {userObject ->
                    // The intent object
                    val intent = Intent(context, ProfileDetail::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    // Pass user object into the profile detail activity
                    intent.putExtra("selectedUserObject", userObject)

                    // Start the activity
                    context.startActivity(intent)
                }
            }

            // Set up on click listener for the video call button
            videoCallButton.setOnClickListener {
                // The intent object
                val intent = Intent(context, VideoChat::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // Let the video chat activity know who is the call receiver (message receiver here)
                intent.putExtra("callReceiver", messageReceiverUserId)

                // Pass chat room id of the chat room between current user and other user to the next activity
                intent.putExtra("chatRoomId", chatRoomId)

                // Start the video chat activity
                context.startActivity(intent)
            }

            // Set up on click listener for the audio call button
            audioCallButton.setOnClickListener {
                // The intent object
                val intent = Intent(context, AudioChat::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // Let the audio chat activity know who is the call receiver (message receiver here in this activity)
                intent.putExtra("callReceiver", messageReceiverUserId)

                // Pass chat room id of the chat room between current user and other user to the next activity
                intent.putExtra("chatRoomId", chatRoomId)

                // Start the audio chat activity
                context.startActivity(intent)
            }
        }
    }

    // ViewHolder for the message option more options
    inner class ViewHolderMessageOptionMoreOptions internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val menuItemLayout : ConstraintLayout = itemView.findViewById(R.id.menuItem)
        private val menuItemIcon : ImageView = itemView.findViewById(R.id.menuItemIcon)
        private val menuItemDescription : TextView = itemView.findViewById(R.id.menuItemDescription)

        // The function to set up menu option row
        fun setUpMenuOptionRow (menuItemDescriptionParam: String, menuItemIconParam: Int, itemOnClickListener: View.OnClickListener) {
            // Load item description into the text view
            menuItemDescription.text = menuItemDescriptionParam

            // Load item icon into the image view
            menuItemIcon.setImageResource(menuItemIconParam)

            // Set up on click listener for the item
            menuItemLayout.setOnClickListener(itemOnClickListener)
        }
    }

    // ViewHolder for the user album
    inner class ViewHolderMessageOptionMessagePhoto internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val image1ImageView: ImageView = itemView.findViewById(R.id.image1ProfileDetail)
        private val image2ImageView: ImageView = itemView.findViewById(R.id.image2ProfileDetail)
        private val image3ImageView: ImageView = itemView.findViewById(R.id.image3ProfileDetail)
        private val image4ImageView: ImageView = itemView.findViewById(R.id.image4ProfileDetail)

        // The function to set up user album row
        fun setUpUserAlbumRow (image1URL: String, image2URL: String, image3URL: String, image4URL: String) {
            // Load images into the ImageView
            if (image1URL != "") {
                Glide.with(context).load(image1URL).into(image1ImageView)
            }
            if (image2URL != "") {
                Glide.with(context).load(image2URL).into(image2ImageView)
            }
            if (image3URL != "") {
                Glide.with(context).load(image3URL).into(image3ImageView)
            }
            if (image4URL != "") {
                Glide.with(context).load(image4URL).into(image4ImageView)
            }

            // Set on click listener for the image view so that it will take user to the activity where the user
            // can see zoomable image
            image1ImageView.setOnClickListener {
                // Call the function
                gotoZoom(image1URL)
            }
            image2ImageView.setOnClickListener {
                // Call the function
                gotoZoom(image2URL)
            }
            image3ImageView.setOnClickListener {
                // Call the function
                gotoZoom(image3URL)
            }
            image4ImageView.setOnClickListener {
                // Call the function
                gotoZoom(image4URL)
            }
        }
    }
    //*********************************** END VIEW HOLDERS FOR THE RECYCLER VIEW ***********************************

    //*********************************** ADDITIONAL FUNCTIONS ***********************************
    // The function which will take user to the activity where user can zoom in and out an image
    fun gotoZoom (imageURL: String) {
        if (imageURL == "") {
            return
        }

        // The intent object
        val intent = Intent(context, ZoomImage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Let the activity know which image to load
        intent.putExtra("imageURLToLoad", imageURL)

        // Start the activity
        context.startActivity(intent)
    }
    //*********************************** ADDITIONAL FUNCTIONS ***********************************

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view : View

        // Based on view type to return the right view holder
        return when (viewType) {
            0 -> {
                // View type 0 is for the header
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.message_option_header, parent, false)

                // Return the view holder
                ViewHolderMessageOptionMenuHeader(view)
            }
            1 -> {
                // View type 1 is for the message option menu
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.menu_row, parent, false)

                // Return the view holder
                ViewHolderMessageOptionMoreOptions(view)
            }
            else -> {
                // view type 3 is for the user album
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_photo_show, parent, false)

                // Return the view holder
                ViewHolderMessageOptionMessagePhoto(view)
            }
        }
    }

    override fun getItemCount(): Int {
        // Get number of rows needed for the user album
        val numOfRowsForMessagePhotos = if (arrayOfMessagePhotos.size % 4 == 0) {
            // If there is no remainder from the division of number of elements with 4, number of rows will be
            // number of elements divided by 4
            arrayOfMessagePhotos.size / 4
        } // Otherwise, it will be number of elements divided by 4 and add 1 into it
        else {
            (arrayOfMessagePhotos.size / 4) + 1
        }

        // Number of rows for the menu will be
        /*
        1.Header
        2. Menu options
        3. Photos
        sum = numOfRowsForMessagePhotos + 4
         */
        return numOfRowsForMessagePhotos + 3
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // First row of the RecyclerView should show the header
        if (position == 0) {
            // Call the function to set up the header row
            (holder as ViewHolderMessageOptionMenuHeader).setUpHeaderRow(messageReceiverUserId)
        }
        // Next row will be the delete conversation button
        else if (position == 1) {
            // Call the function to set up delete button
            (holder as ViewHolderMessageOptionMoreOptions).setUpMenuOptionRow("Delete conversation", R.drawable.ic_baseline_delete_24, View.OnClickListener { })
        }
        // Next row will be the block button
        else if (position == 2) {
            // Block button content
            var blockButtonContent = ""

            // Call the function to check for block status between the currently logged in user and user with specified user
            // id in this activity
            userBlockRepository.checkBlockStatusBetweenCurrentUserAndOtherUser(messageReceiverUserId) {isBlocked ->
                // If user is not block, show the "Block" button
                blockButtonContent = if (!isBlocked) {
                    "Block"
                } else {
                    "Unblock"
                }

                // Call the function to set up block button
                (holder as ViewHolderMessageOptionMoreOptions).setUpMenuOptionRow(blockButtonContent, R.drawable.ic_baseline_block_24, View.OnClickListener {
                    // If content of the block button is "Block", call the function to block a user
                    if (blockButtonContent == "Block") {
                        userBlockRepository.createABlockBetweenCurrentUserAndOtherUser(
                            messageReceiverUserId,
                            "message"
                        ) { isBlocked ->
                            if (isBlocked) {
                                this.notifyDataSetChanged()
                                Toast.makeText(context, "User is blocked", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        userBlockRepository.deleteABlockBetweenCurrentUserAndOtherUser(
                            messageReceiverUserId
                        ) {isDeleted ->
                            if (isDeleted) {
                                this.notifyDataSetChanged()
                                Toast.makeText(context, "User is unblocked", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
            }
        }
        // The rest will show the user album
        else {
            // Check to see how many images remaining in the array
            if (arrayOfMessagePhotos.size - (position - 3) * 4 >= 4) {
                // Get the images
                val image1 = arrayOfMessagePhotos[(position - 3) * 4]
                val image2 = arrayOfMessagePhotos[(position - 3) * 4 + 1]
                val image3 = arrayOfMessagePhotos[(position - 3) * 4 + 2]
                val image4 = arrayOfMessagePhotos[(position - 3) * 4 + 3]

                // If the remaining number of images is greater than or equal to 4, load all images into image view
                (holder as ViewHolderMessageOptionMessagePhoto).setUpUserAlbumRow(image1, image2, image3, image4)
            } // If the remaining number of images in the array is less than 4, just load the remaining in and leave the rest blank
            else {
                // Based on the remaining number of images to decide
                when {
                    arrayOfMessagePhotos.size - ((position - 3) * 4) == 3 -> {
                        // Get the images
                        val image1 = arrayOfMessagePhotos[(position - 3) * 4]
                        val image2 = arrayOfMessagePhotos[(position - 3) * 4 + 1]
                        val image3 = arrayOfMessagePhotos[(position - 3) * 4 + 2]

                        (holder as ViewHolderMessageOptionMessagePhoto).setUpUserAlbumRow(image1, image2,
                            image3, "")
                    }
                    arrayOfMessagePhotos.size - ((position - 3) * 4) == 2 -> {
                        // Get the images
                        val image1 = arrayOfMessagePhotos[(position - 3) * 4]
                        val image2 = arrayOfMessagePhotos[(position - 3) * 4 + 1]

                        (holder as ViewHolderMessageOptionMessagePhoto).setUpUserAlbumRow(image1, image2,
                            "", "")
                    }
                    arrayOfMessagePhotos.size - ((position - 3) * 4) == 1 -> {
                        // Get the images
                        val image1 = arrayOfMessagePhotos[(position - 3) * 4]

                        (holder as ViewHolderMessageOptionMessagePhoto).setUpUserAlbumRow(image1,
                            "", "", "")
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                // First row of the RecyclerView should show the header
                0
            } // Next row will be the delete conversation button
            1 -> {
                1
            }
            // Next row will be the block button
            2 -> {
                1
            }
            // The rest will show the user album
            else -> {
                2
            }
        }
    }
}