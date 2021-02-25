package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Network.User.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Network.Messages.GetMessageRoomIdBetween2UsersService
import com.beta.myhbt_api.Network.RetrofitClientInstance
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Chat.Chat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterSearchUserToChatWith (users: ArrayList<User>, activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of users to show
    private val users = users

    // Activity of the parent activity
    private val activity = activity

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
                getInfoOfCurrentUserAndCheckMessageRoom(user.getId())
            }

            // Load full name into the TextView for the user
            userFullName.text = user.getFullName()

            // Load avatar of the user into the ImageView
            Glide.with(activity)
                .load(user.getAvatarURL())
                .into(userAvatar)
        }
    }

    // The function to get info of current user, check if there is message room between current user and selected or not
    // then take user to the activity where they can start chatting
    fun getInfoOfCurrentUserAndCheckMessageRoom (messageReceiverUserId: String) {
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
                    val body = response.body()
                    print(body)
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user id of the currently logged in user
                    val userId = data["_id"] as String

                    // Call the function to check if there is message room between the 2 users or not
                    // and take user to the activity where the user can start chatting
                    gotoChat(messageReceiverUserId, userId)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to take user to the activity where the user can chat with the selected user
    fun gotoChat (messageReceiverUserId: String, currentUserId: String) {
        // Create the get message room id service
        val getMessageRoomIdBetween2UsersService: GetMessageRoomIdBetween2UsersService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetMessageRoomIdBetween2UsersService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getMessageRoomIdBetween2UsersService.getMessageRoomIddBetween2Users(currentUserId, messageReceiverUserId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    val body = response.body()
                    print(body)
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get status of the response (success or empty)
                    val status = responseBody["status"] as String

                    // Check the status to see if there is message room between the 2 users or not
                    val chatRoomId = if (status == "success") {
                        // If the status is "success", it means that there is chat room between the 2 users
                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get the chat room id
                        data["_id"] as String
                    } else {
                        // If there has not any message room between the 2 users, let the chatRoomId be an empty string
                        ""
                    }

                    // Create the intent which will take user to the activity where the user can chat with the selected user
                    val intent = Intent(activity, Chat::class.java)

                    // Put user id of the message receiver and chat room id into the intent so that next activity
                    // will know which user to send message to and which message room to work with
                    intent.putExtra("chatRoomId", chatRoomId).putExtra("receiverUserId", messageReceiverUserId)

                    // Start the activity
                    activity.startActivity(intent)
                } else {
                    print("Something is not right")
                }
            }
        })
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