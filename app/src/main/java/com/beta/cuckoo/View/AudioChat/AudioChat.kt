package com.beta.cuckoo.View.AudioChat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.NotificationRepositories.NotificationRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.Repository.VideoChatRepository.VideoChatRepository
import com.twilio.video.*
import kotlinx.android.synthetic.main.activity_video_chat.*
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
                        "data",
                        "${chatRoomName}-${userObject.getId()}"
                    ) {
                        // Show the is dialing signal
                        dialStatus.text = "Ringing..."

                        // Start playing the ringing sound
                        mp.start()
                    }
                }
            } else {
                // Otherwise, start the call
                inCallLayout.visibility = View.VISIBLE
                isCallingLayout.visibility = View.INVISIBLE
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
            // To be implemented
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

    //************************************************** WORK WITH REMOTE PARTICIPANTS **************************************************
    private fun addRemoteParticipant(remoteParticipant: RemoteParticipant) {
        /*
         * Start listening for participant events
         */
        remoteParticipant.setListener(remoteParticipantListener)
    }
}