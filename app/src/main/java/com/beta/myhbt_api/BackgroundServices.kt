package com.beta.myhbt_api

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.View.MainMenu
import com.bumptech.glide.Glide
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.nav_header.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BackgroundServices : IntentService (BackgroundServices::class.simpleName) {
    // User object of the currently logged in user
    private lateinit var currentUserObject: User

    // These objects are used for socket.io
    private val gson = Gson()

    lateinit var mSocket: Socket

    override fun onHandleIntent(intent: Intent?) {
        // Call the function to get info of the currently logged in user
        getCurrentUser()

        // Call the function to create notification channel
        createNotificationChannel()
    }

    //************************ DO THINGS WITH THE SOCKET.IO ************************
    // The function to set up socket.IO
    private fun setUpSocketIO () {
        // This address is to connect with the server
        mSocket = IO.socket("http://10.0.2.2:3000")
        //mSocket = IO.socket("https://myhbt-api.herokuapp.com")
        //mSocket = IO.socket("http://localhost:3000")
        mSocket.connect()

        // Bring user into the notification room
        mSocket.emit(
            "jumpInNotificationRoom", gson.toJson(
                hashMapOf(
                    "userId" to currentUserObject.getId()
                )
            )
        )

        // Listen to event of when post of user get commented
        MainMenu.mSocket.on("postGetCommented", onUpdateComment)

        // Listen to event of when message is sent to user
        MainMenu.mSocket.on("messageReceived", onMessageReceived)
    }
    //************************ END WORKING WITH SOCKET.IO ************************

    //************************* CALL BACK FUNCTIONS FOR SOCKET.IO *************************
    // The callback function to update comment when the new one is added to the post
    private var onUpdateComment = Emitter.Listener {
        // New comment object from the server
        val commentObject = it[0]

        val map = HashMap<String, String>()

        val jObject = JSONObject(commentObject.toString())
        val keys: Iterator<*> = jObject.keys()

        while (keys.hasNext()) {
            val key = keys.next() as String
            val value: String = jObject.getString(key)
            map[key] = value
        }

        // Call the function to create notification
        getUserInfoAndCreateNotification(map["fromUser"]!!, map["content"]!!, " commented on your post")
    }

    // The callback function to send notification when message is received
    private var onMessageReceived = Emitter.Listener {
        // New message object from the server
        val messageObject = it[0]

        val map = HashMap<String, String>()

        val jObject = JSONObject(messageObject.toString())
        val keys: Iterator<*> = jObject.keys()

        while (keys.hasNext()) {
            val key = keys.next() as String
            val value: String = jObject.getString(key)
            map[key] = value
        }

        // Call the function to create notification
        getUserInfoAndCreateNotification(map["fromUser"]!!, map["content"]!!, " sent you a message")
    }
    //************************* END CALL BACK FUNCTIONS FOR SOCKET.IO *************************

    //******************************** END CREATE NOTIFICATION SEQUENCE ********************************
    // The function to create a notification channel
    private fun createNotificationChannel () {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification"
            val descriptionText = "Channel for the notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notification_channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // The function to create notification
    private fun createNotification(title: String, content: String) {
        val builder = NotificationCompat.Builder(this, "notification_channel")
            .setSmallIcon(R.drawable.hbtgram1)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    //******************************* GET INFO OF USER BASED ON ID AND CREATE NOTIFICATION *******************************
    // The function to get user info based on user id and create notification
    private fun getUserInfoAndCreateNotification(userId: String, content: String, title: String) {
        // Create the get user info based on id service
        val getUserInfoBasedOnIdService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(
            this
        )!!.create(GetUserInfoBasedOnIdService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserInfoBasedOnIdService.getUserInfoBasedOnId(userId)

        // Perform the call
        call.enqueue(object : Callback<Any> {
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

                    // Get user info from the received data
                    val userInfo = (data["documents"] as List<Map<String, Any>>)[0]

                    // Get user full name
                    val userFullName = userInfo["fullName"] as String

                    // Call the function to create notification
                    createNotification("$userFullName $title", content)
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //******************************* END GET INFO OF USER BASED ON ID AND CREATE NOTIFICATION *******************************

    //******************************* GET INFO OF CURRENT USER SEQUENCE *******************************
    // The function to get info of current user
    fun getCurrentUser() {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // Create the validate token service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(
            applicationContext
        )!!.create(
            GetCurrentlyLoggedInUserInfoService::class.java
        )

        // Create the call object in order to perform the call
        val call: Call<Any> = getCurrentlyLoggedInUserInfoService.getCurrentUserInfo()

        // Perform the call
        call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is data
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Convert user object which is currently a linked tree map into a JSON string
                    val jsUser = gs.toJson(data)

                    // Convert the JSOn string back into User class
                    val userObject = gs.fromJson<User>(jsUser, User::class.java)

                    // Update current user object for this activity
                    currentUserObject = userObject

                    // Call the function to set up socket io
                    setUpSocketIO()
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //******************************* GET INFO OF CURRENT USER SEQUENCE *******************************
}