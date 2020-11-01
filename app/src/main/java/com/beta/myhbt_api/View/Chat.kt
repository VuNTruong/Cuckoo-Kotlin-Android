package com.beta.myhbt_api.View

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Model.Message
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterChat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Chat : AppCompatActivity() {
    private lateinit var mSocket: Socket
    private val gson = Gson()

    // Array of chat messages
    private var chatMessages = ArrayList<Message>()

    // Adapter for the RecyclerView
    private var adapter : RecyclerViewAdapterChat ?= null

    // User id of the receiver
    private var receiverUserId = ""

    // Id of the chat room which contains the current user and the one messaging with
    private var chatRoomId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Get chat room id from previous activity
        chatRoomId = intent.getStringExtra("chatRoomId")!!

        // Get receiver user id from previous activity
        receiverUserId = intent.getStringExtra("receiverUserId")!!

        // Execute the AsyncTask to get info of the message receiver
        GetUserInfoBasedOnUserId().execute(hashMapOf(
            "userId" to receiverUserId,
            "userAvatarImageView" to receiverAvatarChat,
            "userFullNameTextView" to receiverFullNameChat
        ))

        //************************ DO THINGS WITH THE SOCKET.IO ************************
        // Try connecting
        try {
            //This address is the way you can connect to localhost with AVD(Android Virtual Device)
            mSocket = IO.socket("http://10.0.2.2:3000")
            Log.d("success", mSocket.id())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("fail", "Failed to connect")
        }
        mSocket.connect()

        // Bring user into the chat room between this user and the selected user
        mSocket.emit("jumpInChatRoom", gson.toJson(hashMapOf(
            "chatRoomId" to chatRoomId
        )))

        // Listen to event of when new message is sent
        mSocket.on("updateMessage", onUpdateChat)

        // Listen to event of when other user in the chat room is typing
        mSocket.on("typing", onIsTyping)

        // Listen to event of when other user in the chat room is done typing
        mSocket.on("doneTyping", onIsDoneTyping)
        //************************ END WORKING WITH SOCKET.IO ************************

        // Set on click listener for the send image button
        sendImageButtonChatActivity.setOnClickListener {
            // Take user to the activity where the user can pick which photo to send
            val intent = Intent(applicationContext, ChatSendImage::class.java)

            // Pass user id of the message receiver to the next activity as well
            intent.putExtra("messageReceiverUserId", receiverUserId)

            // Start the activity
            startActivity(intent)
        }

        // Let the is typing view to be invisible initially
        isTypingView.visibility = View.INVISIBLE

        // Add text watcher to the message to send content text field so that it will know when text is changing
        messageContentToSend.addTextChangedListener(textWatcher)

        // Instantiate the recycler view
        messageView.layoutManager = LinearLayoutManager(applicationContext)
        messageView.itemAnimator = DefaultItemAnimator()

        // Execute the AsyncTask to get all messages of the user
        GetAllMessagesTask().execute(hashMapOf(
            "chatRoomId" to chatRoomId
        ))

        // Set on click listener for the send message button
        sendMessageButton.setOnClickListener {
            // Execute the AsyncTask to create new message
            GetCurrentUserInfoAndMessage().execute(hashMapOf(
                "messageToSendContentTextView" to messageContentToSend,
                "messageContentToSend" to messageContentToSend.text.toString()
            ))
        }
    }

    // The text watcher which will take action of when there is change in content of the message to send content text field
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Emit event which will let the server know that current user is typing so that the server will
            // let other user in the chat room know that
            mSocket.emit("isTyping", gson.toJson(hashMapOf(
                "chatRoomId" to chatRoomId
            )))

            // If content of the text field is empty, emit event to the server to so that the server will let
            // other user in the chat room know that current user is not typing
            if (messageContentToSend.text.toString() == "") {
                mSocket.emit(
                    "isDoneTyping", gson.toJson(
                        hashMapOf(
                            "chatRoomId" to chatRoomId
                        )
                    )
                )
            }
        }
    }

    // Call back function for socket.io which will update the chat based on signal from the server
    private var onUpdateChat = Emitter.Listener {
        val chat: Message = gson.fromJson(it[0].toString(), Message::class.java)

        // Since this will update the view, it MUST run on the UI thread
        runOnUiThread{
            // Get sender of the message
            val sender = chat.getSender()

            // Get receiver of the message
            val receiver = chat.getReceiver()

            // Get content of the message
            val content = chat.getContent()

            // Get id of the message
            val messageId = chat.getMessageId()

            // Crete message object out of those info
            val messageObject = Message(sender, receiver, content, messageId)

            // Add the received message object to the array of messages
            chatMessages.add(messageObject)

            // Update the RecyclerView
            messageView.adapter!!.notifyDataSetChanged()
        }
    }

    // Call back function which will respond to typing event from the server which indicate that other user in the chat room is typing
    private var onIsTyping = Emitter.Listener {
        // Show the is typing view. Also run this guy on UI thread
        runOnUiThread{
            isTypingView.visibility = View.VISIBLE
        }
    }

    // Call back function which will respond to done typing event from the server which indicate that other user in the chat room has done typing
    private var onIsDoneTyping = Emitter.Listener {
        // Hide the is typing view. Also run this guy on UI thread
        runOnUiThread {
            isTypingView.visibility = View.INVISIBLE
        }
    }

    // AsyncTask for getting info of the message receiver
    inner class GetUserInfoBasedOnUserId : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get user id of the user
            val userId = params[0]!!["userId"] as String

            // The sender avatar image view
            val userAvatarImageView = params[0]!!["userAvatarImageView"] as ImageView

            // The sender full name text view
            val userFullNameTextView = params[0]!!["userFullNameTextView"] as TextView

            // Create the get user info base on id service
            val getUserInfoBasedOnUserIdService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(GetUserInfoBasedOnIdService::class.java)

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
                        Glide.with(applicationContext)
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

    // AsyncTask for getting info of the currently logged in user and get all messages where the user is involved
    inner class GetCurrentUserInfoAndMessage : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Text view which holds content of the message to send. If what to do next is to send the message
            val messageToSendContentTextView = params[0]!!["messageToSendContentTextView"] as TextView

            // Content of the the message to send
            val messageToSendContent = params[0]!!["messageContentToSend"] as String

            // Create the get current user info service
            val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
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

                        // Execute the AsyncTask to create new message
                        CreateNewMessageTask().execute(
                            hashMapOf(
                                "userId" to userId,
                                "messageToSendContentTextView" to messageToSendContentTextView,
                                "messageContentToSend" to messageToSendContent
                            )
                        )
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to get all messages where the user with specified user id is involved in
    inner class GetAllMessagesTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get chat room id
            val chatRoomId = params[0]!!["chatRoomId"] as String

            // Create the get messages service
            val getAllMessagesService: GetAllMessagesOfChatRoomService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
                GetAllMessagesOfChatRoomService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getAllMessagesService.getAllMessagesOfChatRoom(chatRoomId)

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
                        // Body of the response
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get all messages from the data
                        val messages = data["documents"] as List<Map<String, Any>>

                        // Loop through all messages, create objects out of them and add them all to the array of messages
                        for (message in messages) {
                            // Get user id of the sender
                            val senderUserId = message["sender"] as String

                            // Get user if of the receiver
                            val receiverUserId = message["receiver"] as String

                            // Get content of the message
                            val content = message["content"] as String

                            // Get id of the message
                            val messageId = message["_id"] as String

                            // Create object out of those info
                            val messageObject = Message(senderUserId, receiverUserId, content, messageId)

                            // Add the newly created message object into the array of messages
                            chatMessages.add(messageObject)

                            // Update the adapter
                            adapter = RecyclerViewAdapterChat(chatMessages, this@Chat)

                            // Add adapter to the RecyclerView
                            messageView.adapter = adapter
                        }
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to create new message
    inner class CreateNewMessageTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Get user id of the currently logged in user
            val userId = params[0]!!["userId"] as String

            // Text view which holds content of the message to send
            val messageToSendContentTextView = params[0]!!["messageToSendContentTextView"] as TextView

            // Content of the message to send
            val messageToSendContent = params[0]!!["messageContentToSend"] as String

            // Create the create new messages service
            val createNewMessageService: CreateNewMessageService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
                CreateNewMessageService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = createNewMessageService.createNewMessage(userId, receiverUserId, messageToSendContent)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that message is created
                    if (response.body() != null) {
                        // Body of the response
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Create the new message object
                        val newMessageObject = Message(userId, receiverUserId, messageToSendContentTextView.text.toString(), data["_id"] as String)

                        // Add new message object to the array of messages
                        chatMessages.add(newMessageObject)

                        // Update the RecyclerView
                        messageView.adapter!!.notifyDataSetChanged()

                        // Emit event to the server so that the server will let the selected user know that new message has been sent
                        mSocket.emit("newMessage", gson.toJson(hashMapOf(
                            "sender" to userId,
                            "receiver" to receiverUserId,
                            "content" to messageToSendContentTextView.text.toString(),
                            "chatRoomId" to chatRoomId
                        )))

                        // Clear content of the message to send content edit text
                        messageToSendContentTextView.text = ""

                        // Emit event to the server so that the server will let other user in the chat room know that
                        // current user is done typing
                        mSocket.emit(
                            "isDoneTyping", gson.toJson(
                                hashMapOf(
                                    "chatRoomId" to chatRoomId
                                )
                            )
                        )
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }
}
