package com.beta.cuckoo.View.AudioChat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Chronometer
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.NotificationRepositories.NotificationRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.Repository.VideoChatRepository.VideoChatRepository
import com.bumptech.glide.Glide
import com.twilio.video.*
import kotlinx.android.synthetic.main.activity_audio_chat.*
import kotlinx.android.synthetic.main.activity_audio_chat_incoming_call.*
import kotlinx.android.synthetic.main.activity_video_chat.*
import kotlinx.android.synthetic.main.activity_video_chat.isCallingLayout
import java.lang.IllegalStateException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AudioChat : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Video chat repository
    private lateinit var videoChatRepository: VideoChatRepository

    // Notification repository
    private lateinit var notificationRepository: NotificationRepository

    // User repository
    private lateinit var userRepository: UserRepository

    // Chat room name
    private var chatRoomName = "chat-room-go"

    // Call receiver user id
    private var callReceiverUserId = ""

    // The ringtone player
    private lateinit var mp: MediaPlayer

    // Create an audio track
    private var enable = true
    private lateinit var localAudioTrack: LocalAudioTrack

    // The room in which user and peer will be in
    private lateinit var room: Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_chat)

        // Hide the action bar
        supportActionBar!!.hide()

        // Hide the timer which keep track of duration of the call
        callTimer.visibility = View.VISIBLE
        callTimerCountUp.visibility = View.INVISIBLE

        // Call the function to request for permission
        requestPermissionForCameraAndMicrophone()

        // Instantiate media player
        mp = MediaPlayer.create(applicationContext, R.raw.phonecallsound)

        // Get user id of the call receiver
        if (intent.getStringExtra("callReceiver") != null) {
            callReceiverUserId = intent.getStringExtra("callReceiver") as String
        }

        // Get chat room id of the chat room in which 2 users are in
        if (intent.getStringExtra("chatRoomId") != null) {
            chatRoomName = intent.getStringExtra("chatRoomId") as String
        }

        // This one is to finish the activity when call receiver does not accept the call
        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(arg0: Context?, intent: Intent) {
                val action = intent.action
                if (action == "finish") {
                    // Stop the sound
                    mp.stop()

                    // End the room
                    endRoom(chatRoomName)

                    // Finishing the activity
                    finish()
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("finish"))

        // Instantiate user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate notification repository
        notificationRepository = NotificationRepository(executorService, applicationContext)

        // Instantiate video chat repository
        videoChatRepository = VideoChatRepository(executorService, applicationContext)
        
        // Call the function to get info of the call receiver
        getCallReceiverInfo(callReceiverUserId)

        // Set up on click listener for the stop call button
        stopCallButtonAudioChat.setOnClickListener {
            // Call the function to send notification to call receiver and let call receiver know that call has been stopped
            // by caller
            notificationRepository.sendNotificationToAUser(callReceiverUserId, "cancelledCall", "callEnded") {
                // Call the function to end the room
                endRoom(chatRoomName)
            }
        }
    }

    // The function to connect to a chat room
    /*
    At this point, caller initiate the call and call receiver will answer
    When caller call this function, display the is connecting signal
    When call receiver call this function, call is accepted and started
     */
    private fun connectToAChatRoom (roomName: String, userId: String) {
        try {
            // Call the function to get access token for the user
            videoChatRepository.getAccessTokenIntoVideoChatRoom(roomName, userId) { accessToken ->
                // Call the function to create the video chat room
                videoChatRepository.createRoom(roomName) { isExisted, _ ->
                    if (isExisted) {
                        val connectOptions = ConnectOptions.Builder(accessToken)
                            .roomName(roomName)
                            .audioTracks(arrayListOf(localAudioTrack))
                            .build()

                        // Connect to the room
                        room = Video.connect(applicationContext, connectOptions, roomListener)
                    } else {
                        val connectOptions = ConnectOptions.Builder(accessToken)
                            .roomName(roomName)
                            .audioTracks(arrayListOf(localAudioTrack))
                            .build()

                        // Connect to the room
                        room = Video.connect(applicationContext, connectOptions, roomListener)
                    }
                }
            }
        } catch (exception: IllegalStateException) { }
    }

    // The function to end the video chat room
    private fun endRoom (roomName: String) {
        // Call the function to end the room
        videoChatRepository.deleteVideoRoomName(roomName) {isDeleted ->
            if (isDeleted) {
                // Stop the dialing sound
                mp.stop()

                try {
                    // Release audio track
                    localAudioTrack.release()
                } catch (exception: IllegalStateException) {
                    // Finish the activity
                    this.finish()
                }

                // Finish the activity
                this.finish()
            }
        }
    }

    //************************************************** LISTENERS **************************************************
    // Room listener
    private val roomListener = object : Room.Listener {
        /*
        At this point, call is connected
        On caller side, show the is dialing signal
         */
        override fun onConnected(room: Room) {
            // When a user join a room, get info of other user that is already inside room
            // and display their media
            room.remoteParticipants.firstOrNull()?.let { addRemoteParticipant(it) }

            // If there is no one in the room, it means that user of this device is the caller
            // in that case, send notification to call receiver
            // Title will be "video-chat-received" ("title" for now)
            // body will be chat room id in which 2 users will be in if call is accepted as well as user id of the currently logged in user
            if (room.remoteParticipants.size == 0) {
                // Get user id of the currently logged in user
                userRepository.getInfoOfCurrentUser { userObject ->
                    notificationRepository.sendDataNotificationToAUser(
                        callReceiverUserId,
                        "audio-chat-received",
                        "${chatRoomName}-${userObject.getId()}"
                    ) {
                        // Show the is dialing signal
                        callTimer.text = "Ringing..."

                        // Start playing the ringing sound
                        mp.start()
                    }
                }
            } else {
                // Otherwise, start the call
                // Show the call timer
                callTimer.visibility = View.INVISIBLE
                callTimerCountUp.visibility = View.VISIBLE
                callTimerCountUp.start()
            }
        }

        override fun onConnectFailure(room: Room, twilioException: TwilioException) {
            // To be implemented
        }

        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            // To be implemented
        }

        override fun onReconnected(room: Room) {
            // To be implemented
        }

        override fun onDisconnected(room: Room, twilioException: TwilioException?) {
            try {
                // Release audio track
                localAudioTrack.release()
            } catch (exception: IllegalStateException) {
                // Finish the activity
                this@AudioChat.finish()
            }

            // Finish the activity
            this@AudioChat.finish()
        }

        override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
            // Set remote participant listener
            remoteParticipant.setListener(remoteParticipantListener)
        }

        override fun onParticipantDisconnected(room: Room, remoteParticipant: RemoteParticipant) {
            // To be implemented
        }

        override fun onRecordingStarted(room: Room) {
            // To be implemented
        }

        override fun onRecordingStopped(room: Room) {
            // To be implemented
        }
    }

    private val remoteParticipantListener = object : RemoteParticipant.Listener {
        override fun onAudioTrackPublished(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
            // To be implemented
        }

        override fun onAudioTrackUnpublished(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
            // To be implemented
        }

        override fun onAudioTrackSubscribed(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication,
            remoteAudioTrack: RemoteAudioTrack
        ) {
            // Stop the dialing sound
            mp.stop()

            // Start the call timer
            callTimer.visibility = View.INVISIBLE
            callTimerCountUp.visibility = View.VISIBLE
            callTimerCountUp.start()
        }

        override fun onAudioTrackSubscriptionFailed(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication,
            twilioException: TwilioException
        ) {
            // To be implemented
        }

        override fun onAudioTrackUnsubscribed(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication,
            remoteAudioTrack: RemoteAudioTrack
        ) {
            // To be implemented
        }

        override fun onVideoTrackPublished(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
            // To be implemented
        }

        override fun onVideoTrackUnpublished(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
            // To be implemented
        }

        /*
        At this point, remote participant is connected and stop showing is dialing signal
         */
        override fun onVideoTrackSubscribed(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication,
            remoteVideoTrack: RemoteVideoTrack
        ) {
            // Stop the dialing sound
            mp.stop()

            // Hide the is dialing layout
            inCallLayout.visibility = View.VISIBLE
            isCallingLayout.visibility = View.INVISIBLE

            // Start showing video
            remoteVideoView.mirror = false
            remoteVideoTrack.addSink(remoteVideoView)
        }

        override fun onVideoTrackSubscriptionFailed(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication,
            twilioException: TwilioException
        ) {
            // To be implemented
        }

        override fun onVideoTrackUnsubscribed(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication,
            remoteVideoTrack: RemoteVideoTrack
        ) {
            // To be implemented
        }

        override fun onDataTrackPublished(
            remoteParticipant: RemoteParticipant,
            remoteDataTrackPublication: RemoteDataTrackPublication
        ) {
            // To be implemented
        }

        override fun onDataTrackUnpublished(
            remoteParticipant: RemoteParticipant,
            remoteDataTrackPublication: RemoteDataTrackPublication
        ) {
            // To be implemented
        }

        override fun onDataTrackSubscribed(
            remoteParticipant: RemoteParticipant,
            remoteDataTrackPublication: RemoteDataTrackPublication,
            remoteDataTrack: RemoteDataTrack
        ) {
            // To be implemented
        }

        override fun onDataTrackSubscriptionFailed(
            remoteParticipant: RemoteParticipant,
            remoteDataTrackPublication: RemoteDataTrackPublication,
            twilioException: TwilioException
        ) {
            // To be implemented
        }

        override fun onDataTrackUnsubscribed(
            remoteParticipant: RemoteParticipant,
            remoteDataTrackPublication: RemoteDataTrackPublication,
            remoteDataTrack: RemoteDataTrack
        ) {
            // To be implemented
        }

        override fun onAudioTrackEnabled(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
            // To be implemented
        }

        override fun onAudioTrackDisabled(
            remoteParticipant: RemoteParticipant,
            remoteAudioTrackPublication: RemoteAudioTrackPublication
        ) {
            // To be implemented
        }

        override fun onVideoTrackEnabled(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
            // To be implemented
        }

        override fun onVideoTrackDisabled(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication
        ) {
            // To be implemented
        }
    }
    //************************************************** END LISTENERS **************************************************

    //************************************************** INITIAL SET UP **************************************************
    // The function to set up audio
    private fun setUpAudio () {
        // Enable local audio track
        localAudioTrack = LocalAudioTrack.create(applicationContext, enable)!!
    }

    // The function to get info of call receiver
    private fun getCallReceiverInfo(callReceiverUserId: String) {
        // Call the function to get info of the currently logged in user
        userRepository.getUserInfoBasedOnId(callReceiverUserId) { userObject ->
            // Load user full name into text view
            callReceiverNameAudioChat.text = userObject.getFullName()

            // Load user avatar into image view
            Glide.with(applicationContext)
                .load(userObject.getAvatarURL())
                .into(callReceiverAvatarAudioChat)
        }
    }
    //************************************************** END INITIAL SET UP **************************************************

    //************************************************** WORK WITH REMOTE PARTICIPANTS **************************************************
    private fun addRemoteParticipant(remoteParticipant: RemoteParticipant) {
        /*
         * Start listening for participant events
         */
        remoteParticipant.setListener(remoteParticipantListener)
    }
    //************************************************** END WORK WITH REMOTE PARTICIPANTS **************************************************

    //************************************************** REQUEST PERMISSION **************************************************
    // The function to request for permission to use camera and microphone
    private fun requestPermissionForCameraAndMicrophone() {
        // If user need explanation on why the app need to get access to camera and microphone
        // explain it for the user
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, "We need your permission to use microphone and camera", Toast.LENGTH_LONG).show()
        } // If not, start asking for permission
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    // Handle user response to permission
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == 1) {
            var cameraAndMicPermissionGranted = true

            for (grantResult in grantResults) {
                cameraAndMicPermissionGranted = cameraAndMicPermissionGranted and
                        (grantResult == PackageManager.PERMISSION_GRANTED)
            }

            if (cameraAndMicPermissionGranted) {
                // Call the function to set up audio
                setUpAudio()

                // Bring user into room
                userRepository.getInfoOfCurrentUser { userObject ->
                    // Call the function to start connecting
                    connectToAChatRoom(chatRoomName, userObject.getId())
                }
            } else {
                Toast.makeText(this, "We need your permission to use microphone and camera", Toast.LENGTH_LONG).show()
            }
        }
    }
    //************************************************** END REQUEST PERMISSION **************************************************
}