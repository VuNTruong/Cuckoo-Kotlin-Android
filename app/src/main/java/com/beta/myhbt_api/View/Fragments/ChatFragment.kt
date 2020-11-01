package com.beta.myhbt_api.View.Fragments

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetMessageRoomOfUserService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.MessageRoom
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterMessageRoom
import kotlinx.android.synthetic.main.fragment_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment : Fragment() {
    // Array of message room
    private var arrayOfMessageRooms = ArrayList<MessageRoom>()

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterMessageRoom

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the recycler view
        messageRoomView.layoutManager = LinearLayoutManager(this@ChatFragment.requireActivity())
        messageRoomView.itemAnimator = DefaultItemAnimator()

        // Execute the AsyncTask to get info of current user and list of message rooms in which the user is involved
        GetCurrentUserInfoAndMessageRoom().execute()
    }

    // AsyncTask to get info of the currently logged in user
    inner class GetCurrentUserInfoAndMessageRoom : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Create the get current user info service
            val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(this@ChatFragment.requireActivity())!!.create(
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

                        // Execute the AsyncTask to get message room of the currently logged in user
                        GetMessageRoomOfCurrentUserTask().execute(hashMapOf(
                            "currentUserId" to userId
                        ))
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to get message rooms in which the current user is involved
    inner class GetMessageRoomOfCurrentUserTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get user id of the currently logged in user
            val currentUserId = params[0]!!["currentUserId"] as String

            // Create the get message room of user service
            val getMessageRoomOfUserService: GetMessageRoomOfUserService = RetrofitClientInstance.getRetrofitInstance(this@ChatFragment.requireActivity())!!.create(
                GetMessageRoomOfUserService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getMessageRoomOfUserService.getMessageRoomOfUserService(currentUserId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty, there is data
                    if (response.body() != null) {
                        val body = response.body()
                        print(body)
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body (array of message rooms)
                        val data = responseBody["data"] as List<Map<String, Any>>

                        // Loop through that array of message room to create objects out of those info and add them to array of message rooms
                        for (messageRoom in data) {
                            // Get id of the message room
                            val messageRoomId = messageRoom["_id"] as String

                            // Get user1 id
                            val user1Id = messageRoom["user1"] as String

                            // Get user2 id
                            val user2Id = messageRoom["user2"] as String

                            // Create object out of those info
                            val messageRoomObject = MessageRoom(user1Id, user2Id, messageRoomId)

                            // Add the new message room object to the array of message rooms
                            arrayOfMessageRooms.add(messageRoomObject)

                            // Update the adapter
                            adapter = RecyclerViewAdapterMessageRoom(currentUserId, arrayOfMessageRooms, this@ChatFragment.requireActivity())

                            // Add adapter to the RecyclerView
                            messageRoomView.adapter = adapter
                        }
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }
}