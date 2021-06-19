package com.beta.cuckoo.View.VideoChat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.NotificationRepositories.NotificationRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_video_chat_incoming_call.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoChatIncomingCall : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)// Chat room id of the chat room that the 2 users will be in if call is accepted

    // User repository
    private lateinit var userRepository: UserRepository

    // Notification repository
    private lateinit var notificationRepository: NotificationRepository

    // Executor service to perform works in the background
    private var chatRoomId = ""

    // User id of the caller
    private var callerUserId = ""

    // The ringtone player
    private lateinit var mp: MediaPlayer

    // Broadcast receiver in order to receive activity close signal from other activity
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat_incoming_call)

        // Instantiate broadcast receiver
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context?, intent: Intent) {
                val action = intent.action
                if (action == "finish") {
                    // Stop the sound
                    mp.stop()

                    // Finishing the activity
                    finish()
                } else if (action == "callAcceptedOnOneDevice") {
                    // Stop the sound
                    mp.stop()

                    // Finish the activity
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish"))
        registerReceiver(broadcastReceiver, IntentFilter("callAcceptedOnOneDevice"))

        // Get chat room id from previous activity
        chatRoomId = intent.getStringExtra("chatRoomId") as String

        // Get user id of caller from previous activity
        callerUserId = intent.getStringExtra("callerUserId") as String

        // Instantiate media player
        mp = MediaPlayer.create(applicationContext, R.raw.cuckoosound)

        // Instantiate user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate notification repository
        notificationRepository = NotificationRepository(executorService, applicationContext)

        // Hide the action bar
        supportActionBar!!.hide()

        // Call the function to start playing the sound
        playCuckooSound()

        // Call the function to get info of caller
        getCallerInfo(callerUserId)

        // Set up on click listener for the answer button
        acceptCallButton.setOnClickListener {
            // Call the function which will send notification to the currently logged in user himself or herself to let other devices
            // used by currently logged in user know that call has been accepted on current device and shut off ringing sounds on every
            // other devices
            userRepository.getInfoOfCurrentUser {userObject ->
                notificationRepository.sendNotificationToAUser(userObject.getId(), "callAcceptedOnOneDevice", "callAcceptedOnOneDevice") {
                    // Go to the activity where user can start the call
                    // Intent object
                    val intent = Intent(applicationContext, VideoChat::class.java)

                    // Pass chat room id to the video chat activity
                    intent.putExtra("chatRoomId", chatRoomId)

                    // Start the video chat activity
                    startActivity(intent)

                    // Stop the cuckoo sound
                    stopCuckooSound()

                    // Unregister the broadcast receiver
                    unregisterReceiver(broadcastReceiver)

                    // Finish this activity
                    this.finish()
                }
            }
        }

        // Set up on click listener for the decline button
        declineCallButton.setOnClickListener {
            // Call the function to send notification to call receiver and let call receiver know that call has been stopped
            // by caller
            notificationRepository.sendNotificationToAUser(callerUserId, "cancelledCall", "callEnded") {
                // Stop the cuckoo sound
                stopCuckooSound()

                // Unregister the broadcast receiver
                unregisterReceiver(broadcastReceiver)

                // Finish this activity
                this.finish()
            }
        }
    }

    // The function to get info of the caller
    private fun getCallerInfo(callerUserId: String) {
        // Call the function to get info of the currently logged in user
        userRepository.getUserInfoBasedOnId(callerUserId) { userObject ->
            // Load user full name into text view
            callerName.text = userObject.getFullName()

            // Load user avatar into image view
            Glide.with(applicationContext)
                .load(userObject.getAvatarURL())
                .into(callerAvatar)
        }
    }

    // The function to start playing the cuckoo sound
    private fun playCuckooSound () {
        mp.start()
    }

    // The function to stop playing the cuckoo sound
    private fun stopCuckooSound () {
        mp.stop()
    }
}