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
import com.beta.myhbt_api.BackgroundServices
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
    // These objects are used for socket.io
    //private lateinit var mSocket: Socket
    private val gson = Gson()

    // Array of chat messages
    private var chatMessages = ArrayList<Message>()

    // Adapter for the RecyclerView
    private var adapter : RecyclerViewAdapterChat ?= null

    // User id of the receiver
    private var receiverUserId = ""

    // User id of the currently logged in user
    private var currentUserId = ""

    // Id of the chat room which contains the current user and the one messaging with
    // if there has not been a chat room between the 2 users, the app will need to get it from the
    // response and update it here
    private var chatRoomId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Hide the action bar
        supportActionBar!!.hide()

        // Get chat room id from previous activity
        chatRoomId = intent.getStringExtra("chatRoomId")!!

        // Get receiver user id from previous activity
        receiverUserId = intent.getStringExtra("receiverUserId")!!

        // Call the function to get info of the message receiver
        getUserInfoBasedOnId(receiverUserId)

        // CALL THE FUNCTION TO DO THINGS WITH THE SOCKET.IO
        // If there are messages between these 2 users already, chat room id won't be empty. It means that there are already chat room between them
        // and we just need to call the function to set up socket.io
        // if the chat room id is still empty, update it again when first message is sent
        if (chatRoomId != "") {
            setUpSocketIO()
        }

        // Call the function to get info of the currently logged in user (including user id)
        getCurrentUserInfo()

        // Set on click listener for the send image button
        sendImageButtonChatActivity.setOnClickListener {
            // Take user to the activity where the user can pick which photo to send
            val intent = Intent(applicationContext, ChatSendImage::class.java)

            // Pass user id of the message receiver to the next activity as well
            intent.putExtra("messageReceiverUserId", receiverUserId)

            // Pass chat room id of the chat room between current user and other user to the next activity
            intent.putExtra("chatRoomId", chatRoomId)

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

        // Call the function to get all messages of the chat room in which current user and the selected user are in
        getAllMessages()

        // Set on click listener for the send message button
        sendMessageButton.setOnClickListener {
            // Call the function to create new message and send it to the database
            createNewMessage(currentUserId)
        }
    }

    //************************ DO THINGS WITH THE SOCKET.IO ************************
    // The function to do set up things with the socket.io
    fun setUpSocketIO () {
        // Bring user into the chat room between this user and the selected user
        BackgroundServices.mSocket.emit("jumpInChatRoom", gson.toJson(hashMapOf(
            "chatRoomId" to chatRoomId
        )))

        // Listen to event of when new message is sent
        BackgroundServices.mSocket.on("updateMessage", onUpdateChat)

        // Listen to event of when one of the user sent photo to the server
        BackgroundServices.mSocket.on("updateMessageWithPhoto", onUpdateMessageWithPhoto)

        // Listen to event of when other user in the chat room is typing
        BackgroundServices.mSocket.on("typing", onIsTyping)

        // Listen to event of when other user in the chat room is done typing
        BackgroundServices.mSocket.on("doneTyping", onIsDoneTyping)
        //************************ END WORKING WITH SOCKET.IO ************************
    }

    // The text watcher which will take action of when there is change in content of the message to send content text field
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // If chat room id is still blank, don't do anything until first message is sent
            // and chat room id is obtained
            if (chatRoomId == "") {
                return
            }

            // Emit event which will let the server know that current user is typing so that the server will
            // let other user in the chat room know that
            BackgroundServices.mSocket.emit("isTyping", gson.toJson(hashMapOf(
                "chatRoomId" to chatRoomId
            )))

            // If content of the text field is empty, emit event to the server to so that the server will let
            // other user in the chat room know that current user is not typing
            if (messageContentToSend.text.toString() == "") {
                BackgroundServices.mSocket.emit(
                    "isDoneTyping", gson.toJson(
                        hashMapOf(
                            "chatRoomId" to chatRoomId
                        )
                    )
                )
            }
        }
    }

    //************************* CALL BACK FUNCTIONS FOR SOCKET.IO *************************
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

            // Call the function to scroll the end of the message view
            gotoEnd()
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

    // Call back function which will respond to event of when one of the user send photo
    private var onUpdateMessageWithPhoto = Emitter.Listener {
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

            // Call the function to scroll the end of the message view
            gotoEnd()
        }
    }

    //************************* END CALL BACK FUNCTIONS FOR SOCKET.IO *************************
    // The function to get info of the message receiver
    private fun getUserInfoBasedOnId (messageReceiverUserId: String) {
        // Create the get user info base on id service
        val getUserInfoBasedOnUserIdService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(GetUserInfoBasedOnIdService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserInfoBasedOnUserIdService.getUserInfoBasedOnId(messageReceiverUserId)

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
                    val fullName = userInfo["fullName"] as String

                    // Get avatar URL of the sender
                    val senderAvatarURL = userInfo["avatarURL"] as String

                    // Load avatar into the ImageView
                    Glide.with(applicationContext)
                        .load(senderAvatarURL)
                        .into(receiverAvatarChat)

                    // Load full name into the TextView
                    receiverFullNameChat.text = fullName
                }
            }
        })
    }

    //*********************************** GET MESSAGES SEQUENCE ***********************************
    // The function to get all messages of the chat room in which current user and the selected user are in
    private fun getAllMessages () {
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
                    // Body of the response
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get all messages from the data
                    val messages = data["documents"] as ArrayList<Message>

                    // Set the array of messages of this class to be the array of messages obtained from the database
                    chatMessages = messages

                    // Update the adapter
                    adapter = RecyclerViewAdapterChat(chatMessages, this@Chat)

                    // Add adapter to the RecyclerView
                    messageView.adapter = adapter

                    // Call the function to scroll to the end of the message view
                    gotoEnd()
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //*********************************** END GET MESSAGES SEQUENCE ***********************************

    //************************* GET INFO OF CURRENTLY LOGGED IN USER SEQUENCE *************************
    // The function to get info of the currently logged in user
    private fun getCurrentUserInfo() {
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
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user id of the currently logged in user
                    val userId = data["_id"] as String

                    // Update user id for the currently logged in user
                    currentUserId = userId
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //************************* END GET INFO OF CURRENTLY LOGGED IN USER SEQUENCE *************************

    //************************* SEND MESSAGE SEQUENCE *************************
    // The function to create new message and send it to the database
    private fun createNewMessage (currentUserId: String) {
        // Create the create new messages service
        val createNewMessageService: CreateNewMessageService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            CreateNewMessageService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = createNewMessageService.createNewMessage(currentUserId, receiverUserId, messageContentToSend.text.toString())

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

                    // If message is sent for the first time, call the function to set up socket.io for realtime update
                    // if this happens, chat room id of this activity will be empty. We need to check it
                    if (chatRoomId == "") {
                        // Set the current chat room to be the one that contain the message that just been sent
                        // This is very important especially when message is sent at the first time
                        chatRoomId = data["chatRoomId"] as String

                        // Call the function to re-setup the socket.io since the chat room id is now obtained and updated
                        setUpSocketIO()
                    }

                    //---------------------------- Update UI on the server side (socket.io) ----------------------------
                    // Emit event to the server so that the server will let the selected user know that new message has been sent
                    BackgroundServices.mSocket.emit("newMessage", gson.toJson(hashMapOf(
                        "sender" to currentUserId,
                        "receiver" to receiverUserId,
                        "content" to messageContentToSend.text.toString(),
                        "messageId" to data["_id"] as String,
                        "chatRoomId" to chatRoomId
                    )))

                    // Emit event to the server so that the server will let other user in the chat room know that
                    // current user is done typing
                    BackgroundServices.mSocket.emit(
                        "isDoneTyping", gson.toJson(
                            hashMapOf(
                                "chatRoomId" to chatRoomId
                            )
                        )
                    )
                    //---------------------------- End update UI on the server side (socket.io) ----------------------------

                    //---------------------------- Update UI on the app side ----------------------------
                    // Create the new message object
                    val newMessageObject = Message(currentUserId, receiverUserId, messageContentToSend.text.toString(), data["_id"] as String)

                    // Add new message object to the array of messages in this app
                    chatMessages.add(newMessageObject)

                    // Update the RecyclerView
                    messageView.adapter!!.notifyDataSetChanged()

                    // Call the function to scroll to the end of the message view
                    // we may not want user to scroll every time new message is sent
                    gotoEnd()

                    // Clear content of the message to send content edit text
                    messageContentToSend.setText("")
                    //---------------------------- End update UI on the app side ----------------------------

                } else {
                    print("Something is not right")
                }
            }
        })

    }
    //************************* END SEND MESSAGE SEQUENCE *************************

    //************************* SUPPLEMENTAL FUNCTIONS *************************
    fun gotoEnd () {
        // The function to roll to the end of the message view
        messageView.scrollToPosition(chatMessages.size - 1)
    }
}
