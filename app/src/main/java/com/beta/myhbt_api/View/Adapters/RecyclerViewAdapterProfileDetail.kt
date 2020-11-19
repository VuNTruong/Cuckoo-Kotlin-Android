package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Chat
import com.beta.myhbt_api.View.HBTGramPostDetail
import com.beta.myhbt_api.View.MainMenu
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterProfileDetail (arrayOfPhotos: ArrayList<HBTGramPostPhoto>, userObject: User, activity: Activity, currentUser: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of photos created by the user
    private val arrayOfPhotos = arrayOfPhotos

    // User object of the selected user
    private val userObject = userObject

    // Activity of the parent activity
    private val activity = activity

    // The user object is current user or not? (this var will keep track of that)
    private val currentUser = currentUser

    //*********************************** VIEW HOLDERS FOR THE RECYCLER VIEW ***********************************
    // ViewHolder for the profile detail page header
    inner class ViewHolderProfileDetailHeader internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val userAvatar : ImageView = itemView.findViewById(R.id.userAvatarProfileDetail)
        private val userCover : ImageView = itemView.findViewById(R.id.profileDetailCoverPhoto)
        private val userFullName : TextView = itemView.findViewById(R.id.userFullNameProfileDetail)
        private val bio : TextView = itemView.findViewById(R.id.userBioProfileDetail)
        private val numOfPosts : TextView = itemView.findViewById(R.id.numOfPosts)
        private val numOfFollowers : TextView = itemView.findViewById(R.id.numOfFollowers)
        private val numOfFollowing : TextView = itemView.findViewById(R.id.numOfFollowing)

        // The function to set up header row for the profile detail page
        fun setUpProfileDetailHeaderPage (userObject: User) {
            // Load full name into the TextView
            userFullName.text = userObject.getFullName()

            // Load avatar of the user into the ImageView
            Glide.with(activity)
                .load(userObject.getAvatarURL())
                .into(userAvatar)

            // Load cover photo into the ImageView
            Glide.with(activity)
                .load(userObject.getCoverURL())
                .into(userCover)

            // Call the function to get number of followers of the user
            getNumOfFollowers(userObject.getId(), numOfFollowers)

            // Call the function to get number of followings of the user
            getNumOfFollowings(userObject.getId(), numOfFollowing)

            // Call the function to get number of posts of the user
            getNumOfPosts(userObject.getId(), numOfPosts)

            // Call the function to get bio of the user
            getBio(userObject.getId(), bio)
        }
    }

    // ViewHolder for the edit profile button
    inner class ViewHolderEditProfileButton internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val editProfileButton : Button = itemView.findViewById(R.id.editProfileProfileDetail)

        // The function to set up the edit profile button
        fun setUpEditProfileButton () {
            // Add on click listener to the edit profile button which will take user to the activity where the user can edit profile
            editProfileButton.setOnClickListener {
                // Take user to the activity where the user can edit profile
                // it is the main menu, but we will let the activity know that the previous activity is this one
                val intent = Intent(activity, MainMenu::class.java)

                // Pass name of this activity to the main menu so that the menu will know to load the profile settings
                intent.putExtra("previousActivityName", "profileDetailPage")

                // Start the activity
                activity.startActivity(intent)
            }
        }
    }

    // ViewHolder for the follow/message button
    inner class ViewHolderFollowMessageButton internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val followUnfollowButton : Button = itemView.findViewById(R.id.followUnfollowButtonProfileDetail)
        private val messageButton : Button = itemView.findViewById(R.id.sendMessageButtonProfileDetail)

        // The function to set up the follow/message button
        fun setUpFollowMessageButton () {
            // Set on click listener for the follow/unfollow button
            followUnfollowButton.setOnClickListener {
                // Call the function to create follow for the user
            }

            // Set on click listener for the message button
            messageButton.setOnClickListener {
                // Call the function to take user to the activity where the user can start chatting
                getInfoOfCurrentUserAndGotoChat()
            }
        }
    }

    // ViewHolder for the user album
    inner class ViewHolderUserAlbum internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val image1ImageView: ImageView = itemView.findViewById(R.id.image1ProfileDetail)
        private val image2ImageView: ImageView = itemView.findViewById(R.id.image2ProfileDetail)
        private val image3ImageView: ImageView = itemView.findViewById(R.id.image3ProfileDetail)
        private val image4ImageView: ImageView = itemView.findViewById(R.id.image4ProfileDetail)

        // The function to set up user album row
        fun setUpUserAlbumRow (image1URL: String, image2URL: String, image3URL: String, image4URL: String,
                                image1PostId: String, image2PostId: String, image3PostId: String, image4PostId: String) {
            // Load images into the ImageView
            if (image1URL != "") {
                Glide.with(activity).load(image1URL).into(image1ImageView)
            }
            if (image2URL != "") {
                Glide.with(activity).load(image2URL).into(image2ImageView)
            }
            if (image3URL != "") {
                Glide.with(activity).load(image3URL).into(image3ImageView)
            }
            if (image4URL != "") {
                Glide.with(activity).load(image4URL).into(image4ImageView)
            }

            // Set on click listener for the image view so that it will take user to the activity where the user
            // can see post detail of the post associated with the photo
            image1ImageView.setOnClickListener {
                // Call the function
                getPostObjectBasedOnIdAndGotoPostDetail(image1PostId)
            }
            image2ImageView.setOnClickListener {
                // Call the function
                getPostObjectBasedOnIdAndGotoPostDetail(image2PostId)
            }
            image3ImageView.setOnClickListener {
                // Call the function
                getPostObjectBasedOnIdAndGotoPostDetail(image3PostId)
            }
            image4ImageView.setOnClickListener {
                // Call the function
                getPostObjectBasedOnIdAndGotoPostDetail(image3PostId)
            }
        }
    }
    //*********************************** END VIEW HOLDERS FOR THE RECYCLER VIEW ***********************************

    //*********************************** GET USER INFO SEQUENCE ***********************************
    /*
    In this sequence, we will get these info
    1. User bio
    2. Number of followers
    3. Number of followings
    4. Number of posts
     */
    // The function to get user info based on id
    fun getBio(userId: String, userBioTextView: TextView) {
        // Create the get user info service
        val getUserInfoService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetUserInfoBasedOnIdService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserInfoService.getUserInfoBasedOnId(userId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is data
                if (response.body() != null) {
                    val body = response.body()
                    print(body)
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    // Get obtain user data from that
                    val data = (((responseBody["data"] as Map<String, Any>)["documents"]) as List<Map<String, Any>>)[0]

                    // Load bio of the user
                    val userBio = data["description"] as String

                    // Load user info into the TextView
                    userBioTextView.text = userBio
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to get number of followers of the user
    fun getNumOfFollowers (userId: String, numOfFollowerTextView: TextView) {
        // Create the service for getting array of followers (we will get number of followers based on that)
        val getArrayOfFollowersService: GeteFollowerService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GeteFollowerService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getArrayOfFollowersService.getFollowers(userId)

        // Perform the call
        call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("There seem to be an error ${t.stackTrace}")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is data
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get number of followers
                    val numOfFollowers = (responseBody["results"] as Double).toInt()

                    // Load number of followers into the text view
                    numOfFollowerTextView.text = "$numOfFollowers"
                }
            }

        })
    }

    // The function to get number of following
    fun getNumOfFollowings (userId: String, numOfFollowingTextView: TextView) {
        // Create the service for getting number of followings
        val getArrayOfFollowingService: GeteFollowingService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GeteFollowingService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getArrayOfFollowingService.getFollowings(userId)

        // Perform the call
        call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("There seem to be an error ${t.stackTrace}")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is data
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get number of followings
                    val numOfFollowings = (responseBody["results"] as Double).toInt()

                    // Load number of followers into the text view
                    numOfFollowingTextView.text = "$numOfFollowings"
                }
            }
        })
    }

    // The function to get number of posts created by the user
    fun getNumOfPosts (userId: String, numOfPostsTextView: TextView) {
        // Create the service for getting number of posts
        val getPostsOfUserService: GetPostsOfUserService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetPostsOfUserService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getPostsOfUserService.getPostsOfUser(userId)

        // Perform the call
        call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("There seem to be an error ${t.stackTrace}")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is data
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get number of posts
                    val numOfPosts = (responseBody["results"] as Double).toInt()

                    // Load number of posts into the text view
                    numOfPostsTextView.text = "$numOfPosts"
                }
            }
        })
    }
    //*********************************** END GET USER INFO SEQUENCE ***********************************

    //*********************************** GO TO CHAT SEQUENCE ***********************************
    /*
    Do 3 things for this sequence
    1. Get info of the current user
    2. Get message room between current user and the selected user
    3. Go to the chat activity
     */

    // The function to get info of the current user
    fun getInfoOfCurrentUserAndGotoChat () {
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

                    // Get user id in the database of the currently logged in user
                    val userId = data["_id"] as String

                    // Call the function to get message room id between 2 users and go to chat activity
                    checkChatRoomBetween2Users(userId, userObject.getId())
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to check for chat room between the 2 users
    fun checkChatRoomBetween2Users (currentUserId: String, otherUserId: String) {
        // Create the get chat room between 2 users service
        val getChatRoomIdBetween2UsersService: GetMessageRoomIdBetween2UsersService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetMessageRoomIdBetween2UsersService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getChatRoomIdBetween2UsersService.getMessageRoomIddBetween2Users(currentUserId, otherUserId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("There seem be be an error ${t.stackTrace}")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get status of the call (it can be either empty or success)
                    val status = responseBody["status"] as String

                    // If the status is success, get message room id and pass it to the next view controller
                    if (status == "success") {
                        // Get data of the response
                        val data = responseBody["data"] as Map<String, Any>

                        // Chat chat room id
                        val chatRoomId = data["_id"] as String

                        // Call the function and go to chat activity and let it know the chat room id as well
                        gotoChat(userObject.getId(), chatRoomId)
                    } // Otherwise, go to the chat activity and let the chat room id be blank
                    else {
                        gotoChat(userObject.getId(), "")
                    }
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function which will take user to the activity where the user can start chatting
    fun gotoChat (messageReceiverUserId: String, messageRoomId: String) {
        // The intent object
        val intent = Intent(activity, Chat::class.java)

        // Pass info to the chat activity
        intent.putExtra("chatRoomId", messageRoomId)
        intent.putExtra("receiverUserId", messageReceiverUserId)

        // Start the activity
        activity.startActivity(intent)
    }
    //*********************************** END GO TO CHAT SEQUENCE ***********************************

    //*********************************** GO TO POST DETAIL SEQUENCE ***********************************
    /*
    In this sequence, we will do 2 things
    1. Get post object based on the specified id
    2. Go to the post detail activity
     */

    // The function to get post object based on the specified post id
    fun getPostObjectBasedOnIdAndGotoPostDetail (postId: String) {
        // If the post detail is empty, get out of the sequence
        if (postId == "") {
            return
        }

        // Create the get post based on id service
        val getPostBasedOnIdService: GetPostBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetPostBasedOnIdService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getPostBasedOnIdService.getPostBasedOnId(postId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("There seem be be an error ${t.stackTrace}")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response
                    val data = (((responseBody["data"] as Map<String, Any>)["documents"]) as ArrayList<Map<String, Any>>)[0]

                    // In order to prevent us from encountering the class cast exception, we need to do the following
                    // Create the GSON object
                    val gs = Gson()

                    // Convert a linked tree map into a JSON string
                    val jsPost = gs.toJson(data)

                    // Convert the JSOn string back into HBTGramPost class
                    val hbtGramPostModel = gs.fromJson<HBTGramPost>(jsPost, HBTGramPost::class.java)

                    // Call the function which will take user to the activity where the user can see post detail of the post with specified id
                    gotoPostDetail(hbtGramPostModel)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function which will take user to the activity where the user can see post detail of the post with specified user id
    fun gotoPostDetail (postObject: HBTGramPost) {
        // Intent object
        val intent = Intent(activity, HBTGramPostDetail::class.java)

        // Pass the post object to the post detail view controller
        intent.putExtra("selectedPostObject", postObject)

        // Start the activity
        activity.startActivity(intent)
    }
    //*********************************** END GO TO POST DETAIL SEQUENCE ***********************************

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view : View

        // Based on view type to return the right view holder
        return when (viewType) {
            0 -> {
                // View type 0 is for the header
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_header, parent, false)

                // Return the view holder
                ViewHolderProfileDetailHeader(view)
            }
            1 -> {
                // View type 1 is for the edit profile button
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_edit_profile, parent, false)

                // Return the view holder
                ViewHolderEditProfileButton(view)
            }
            2 -> {
                // View type 2 is for the follow/unfollow button and message button
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_send_message_and_follow, parent, false)

                // Return the view holder
                ViewHolderFollowMessageButton(view)
            }
            else -> {
                // view type 3 is for the user album
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_photo_show, parent, false)

                // Return the view holder
                ViewHolderUserAlbum(view)
            }
        }
    }

    override fun getItemCount(): Int {
        // Get number of rows needed for the user album
        val numOfRowsForUserAlbum = if (arrayOfPhotos.size % 4 == 0) {
            // If there is no remainder from the division of number of elements with 4, number of rows will be
            // number of elements divided by 4
            arrayOfPhotos.size / 4
        } // Otherwise, it will be number of elements divided by 4 and add 1 into it
        else {
            (arrayOfPhotos.size / 4) + 1
        }

        // Number of rows needed for this activity will be 2 + number of rows needed for the user album
        return numOfRowsForUserAlbum + 2
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // First row of the RecyclerView should show the header
        if (position == 0) {
            // Call the function to set up the header row
            (holder as ViewHolderProfileDetailHeader).setUpProfileDetailHeaderPage(userObject)
        }
        // Next row will be either follow and message button or edit profile button
        // this based on if the user object is the current user or not, we need to check
        else if (position == 1) {
            // Check to see if the user object is the current user or not
            if (currentUser) {
                // If it is the current user, the row will show the edit profile button
                (holder as ViewHolderEditProfileButton).setUpEditProfileButton()
            } // Otherwise, it is other user, show the follow, message button
            else {
                (holder as ViewHolderFollowMessageButton).setUpFollowMessageButton()
            }
        }
        // The rest will show the user album
        else {
            // Check to see how many images remaining in the array
            if (arrayOfPhotos.size - (position - 2) * 4 >= 4) {
                // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                val jsImage1 = gs.toJson(arrayOfPhotos[(position - 2) * 4])
                val jsImage2 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 1])
                val jsImage3 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 2])
                val jsImage4 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 3])

                // Convert the JSOn string back into HBTGramPostPhoto class
                val hbtGramPostPhotoModelImage1 = gs.fromJson<HBTGramPostPhoto>(jsImage1, HBTGramPostPhoto::class.java)
                val hbtGramPostPhotoModelImage2 = gs.fromJson<HBTGramPostPhoto>(jsImage2, HBTGramPostPhoto::class.java)
                val hbtGramPostPhotoModelImage3 = gs.fromJson<HBTGramPostPhoto>(jsImage3, HBTGramPostPhoto::class.java)
                val hbtGramPostPhotoModelImage4 = gs.fromJson<HBTGramPostPhoto>(jsImage4, HBTGramPostPhoto::class.java)

                // If the remaining number of images is greater than or equal to 4, load all images into image view
                (holder as ViewHolderUserAlbum).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), hbtGramPostPhotoModelImage2.getImageURL(),
                    hbtGramPostPhotoModelImage3.getImageURL(), hbtGramPostPhotoModelImage4.getImageURL(),
                    hbtGramPostPhotoModelImage1.getPostId(), hbtGramPostPhotoModelImage2.getPostId(),
                    hbtGramPostPhotoModelImage3.getPostId(), hbtGramPostPhotoModelImage4.getPostId())
            } // If the remaining number of images in the array is less than 4, just load the remaining in and leave the rest blank
            else {
                // Based on the remaining number of images to decide
                when {
                    arrayOfPhotos.size - ((position - 2) * 4) == 3 -> {
                        // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                        val jsImage1 = gs.toJson(arrayOfPhotos[(position - 2) * 4])
                        val jsImage2 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 1])
                        val jsImage3 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 2])

                        // Convert the JSOn string back into HBTGramPostPhoto class
                        val hbtGramPostPhotoModelImage1 = gs.fromJson<HBTGramPostPhoto>(jsImage1, HBTGramPostPhoto::class.java)
                        val hbtGramPostPhotoModelImage2 = gs.fromJson<HBTGramPostPhoto>(jsImage2, HBTGramPostPhoto::class.java)
                        val hbtGramPostPhotoModelImage3 = gs.fromJson<HBTGramPostPhoto>(jsImage3, HBTGramPostPhoto::class.java)

                        (holder as ViewHolderUserAlbum).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), hbtGramPostPhotoModelImage2.getImageURL(),
                            hbtGramPostPhotoModelImage3.getImageURL(), "",
                            hbtGramPostPhotoModelImage1.getPostId(), hbtGramPostPhotoModelImage2.getPostId(), hbtGramPostPhotoModelImage3.getPostId(), "")
                    }
                    arrayOfPhotos.size - ((position - 2) * 4) == 2 -> {
                        // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                        val jsImage1 = gs.toJson(arrayOfPhotos[(position - 2) * 4])
                        val jsImage2 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 1])

                        // Convert the JSOn string back into HBTGramPostPhoto class
                        val hbtGramPostPhotoModelImage1 = gs.fromJson<HBTGramPostPhoto>(jsImage1, HBTGramPostPhoto::class.java)
                        val hbtGramPostPhotoModelImage2 = gs.fromJson<HBTGramPostPhoto>(jsImage2, HBTGramPostPhoto::class.java)

                        (holder as ViewHolderUserAlbum).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), hbtGramPostPhotoModelImage2.getImageURL(), "", "",
                            hbtGramPostPhotoModelImage1.getPostId(), hbtGramPostPhotoModelImage2.getPostId(), "", "")
                    }
                    arrayOfPhotos.size - ((position - 2) * 4) == 1 -> {
                        // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                        val jsImage1 = gs.toJson(arrayOfPhotos[(position - 2) * 4])

                        // Convert the JSOn string back into HBTGramPostPhoto class
                        val hbtGramPostPhotoModelImage1 = gs.fromJson<HBTGramPostPhoto>(jsImage1, HBTGramPostPhoto::class.java)

                        (holder as ViewHolderUserAlbum).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), "", "", "",
                            hbtGramPostPhotoModelImage1.getPostId(), "", "", "")
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            // First row of the RecyclerView should show the header
            0
        } else if (position == 1) {
            // Next row will be either follow and message button or edit profile button
            // this based on if the user object is the current user or not, we need to check
            // Check to see if the user object is the current user or not
            if (currentUser) {
                // If it is the current user, the row will show the edit profile button
                1
            } // Otherwise, it is other user, show the follow, message button
            else {
                2
            }
        } // The rest will show the user album
        else {
            3
        }
    }
}