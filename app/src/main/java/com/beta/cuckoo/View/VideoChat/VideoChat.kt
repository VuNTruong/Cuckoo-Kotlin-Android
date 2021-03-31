package com.beta.cuckoo.View.VideoChat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.NotificationRepositories.NotificationRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.Repository.VideoChatRepository.VideoChatRepository
import com.bumptech.glide.Glide
import com.twilio.video.*
import kotlinx.android.synthetic.main.activity_video_chat.*
import kotlinx.android.synthetic.main.profile_page_item.*
import tvi.webrtc.Camera1Enumerator
import tvi.webrtc.Camera2Capturer
import tvi.webrtc.Camera2Enumerator
import java.lang.IllegalStateException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.jar.Manifest

class VideoChat : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Video chat repository
    private lateinit var videoChatRepository: VideoChatRepository

    // User repository
    private lateinit var userRepository: UserRepository

    // Chat room name
    private var chatRoomName = "chat-room-go"

    // Call receiver user id
    private var callReceiverUserId = ""

    // The ringtone player
    private lateinit var mp: MediaPlayer

    // Notification repository
    private lateinit var notificationRepository: NotificationRepository

    // Create an audio track
    private var enable = true
    private lateinit var localAudioTrack: LocalAudioTrack

    // The room in which user and peer will be in
    private lateinit var room: Room

    // Back camera id
    private val backCameraId by lazy {
        val camera1Enumerator = Camera1Enumerator()
        val cameraId = camera1Enumerator.deviceNames.find { camera1Enumerator.isBackFacing(it) }
        requireNotNull(cameraId)
    }

    // Front camera id
    private val frontCameraId by lazy {
        val camera1Enumerator = Camera1Enumerator()
        val cameraId = camera1Enumerator.deviceNames.find { camera1Enumerator.isFrontFacing(it) }
        requireNotNull(cameraId)
    }

    // Local video track
    private lateinit var localVideoTrack: LocalVideoTrack

    // The function which will keep track of which camera is being used
    private var isUsingCamera = "back"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat)

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

        // Initially, show the is calling layout
        isCallingLayout.visibility = View.VISIBLE
        inCallLayout.visibility = View.INVISIBLE

        // Instantiate video chat repository
        videoChatRepository = VideoChatRepository(executorService, applicationContext)

        // Instantiate notification repository
        notificationRepository = NotificationRepository(executorService, applicationContext)

        // Instantiate user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Check and ask for user permission
        // We will need sound and camera permission
        if ((ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) ||
            (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)) {
            // Call the function to ask for permission
            requestPermissionForCameraAndMicrophone()
        } else {
            // Call the function to set up camera capture
            setUpCameraCapture()

            // Call the function to set up audio
            setUpAudio()
        }

        // Set up on click listener for the rotate camera button
        rotateCameraButton.setOnClickListener {
            // Call the function to rotate camera
            switchCamera()
        }

        // Set up on click listener for the switch mic button
        micSwitchButton.setOnClickListener {
            // Call the function to do things for the mic switch
            micSwitch()
        }

        // Set up on click listener for the camera switch button
        cameraSwitchButton.setOnClickListener {
            // Call the function to do things for the camera switch
            cameraSwitch()
        }

        // Bring user into room
        userRepository.getInfoOfCurrentUser { userObject ->
            // Call the function to start connecting
            connectToAChatRoom(chatRoomName, userObject.getId())
        }

        // Call the function to get info of call receiver
        userRepository.getUserInfoBasedOnId(callReceiverUserId) {userObject ->
            // Load user full name into text view
            callReceiverName.text = userObject.getFullName()

            // Load user avatar into image view
            Glide.with(applicationContext)
                .load(userObject.getAvatarURL())
                .into(callReceiverAvatar)
        }

        // Set up on click listener for the end button (during call)
        endButton.setOnClickListener {
            // Call the function to end the room
            endRoom(chatRoomName)
        }

        // Set up on click listener for the stop call button (while dialing)
        stopCallButton.setOnClickListener {
            // Call the function to send notification to call receiver and let call receiver know that call has been stopped
            // by caller
            notificationRepository.sendNotificationToAUser(callReceiverUserId, "cancelledCall", "callEnded") {
                // Call the function to end the room
                endRoom(chatRoomName)
            }
        }
    }

    //************************************************** VIDEO CHATTING TOOLS **************************************************
    // The function to switch camera
    private fun switchCamera () {
        // Check to see which one is being used and switch it
        if (isUsingCamera == "front") {
            isUsingCamera = "back"
        } else if (isUsingCamera == "back") {
            isUsingCamera = "front"
        }

        // Call the function to set up camera again
        rotateCameraAndReset()
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
                            .videoTracks(arrayListOf(localVideoTrack))
                            .build()

                        // Connect to the room
                        room = Video.connect(applicationContext, connectOptions, roomListener)
                    } else {
                        val connectOptions = ConnectOptions.Builder(accessToken)
                            .roomName(roomName)
                            .audioTracks(arrayListOf(localAudioTrack))
                            .videoTracks(arrayListOf(localVideoTrack))
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
                    // Remove video sink
                    localVideoTrack.removeSink(localVideoView)

                    // Release video track
                    localVideoTrack.release()

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
    //************************************************** END VIDEO CHATTING TOOLS **************************************************

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
                // Remove video sink
                localVideoTrack.removeSink(localVideoView)

                // Release video track
                localVideoTrack.release()
            } catch (exception: IllegalStateException) {
                // Finish the activity
                this@VideoChat.finish()
            }

            // Finish the activity
            this@VideoChat.finish()
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
         * Add participant renderer
         */
        remoteParticipant.remoteVideoTracks.firstOrNull()?.let { remoteVideoTrackPublication ->
            if (remoteVideoTrackPublication.isTrackSubscribed) {
                remoteVideoTrackPublication.remoteVideoTrack?.let { addRemoteParticipantVideo(it) }
            }
        }

        /*
         * Start listening for participant events
         */
        remoteParticipant.setListener(remoteParticipantListener)
    }

    /*
     * Set primary view as renderer for participant video track
     */
    private fun addRemoteParticipantVideo(videoTrack: VideoTrack) {
        remoteVideoView.mirror = true
        videoTrack.addSink(remoteVideoView)
    }
    //************************************************** END WORK WITH REMOTE PARTICIPANTS **************************************************

    //************************************************** REQUEST PERMISSION **************************************************
    // The function to request for permission to use camera and microphone
    private fun requestPermissionForCameraAndMicrophone() {
        // If user need explanation on why the app need to get access to camera and microphone
        // explain it for the user
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this, "We need your permission to use microphone and camera", Toast.LENGTH_LONG).show()
        } // If not, start asking for permission
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO), 1)
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
                // Call the function to set up camera capture
                setUpCameraCapture()

                // Call the function to set up audio
                setUpAudio()
            } else {
                Toast.makeText(this, "We need your permission to use microphone and camera", Toast.LENGTH_LONG).show()
            }
        }
    }
    //************************************************** END REQUEST PERMISSION **************************************************

    //************************************************** INITIAL SET UP **************************************************
    // The function to set up audio
    private fun setUpAudio () {
        // Enable local audio track
        localAudioTrack = LocalAudioTrack.create(applicationContext, enable)!!
    }

    // The function to set up camera capturing session
    private fun setUpCameraCapture () {
        if (isUsingCamera == "front") {
            if (frontCameraId != "") {
                // Create the CameraCapturer with the front camera
                val cameraCapturer = CameraCapturer(applicationContext, frontCameraId)

                // Create a video track
                localVideoTrack =
                    LocalVideoTrack.create(applicationContext, enable, cameraCapturer)!!

                // Front camera should be mirrored
                localVideoView.mirror = true

                // Render a local video track to preview camera
                localVideoTrack.addSink(localVideoView)
                localVideoTrack.addSink(localVideoViewIsDialing)
            }
        } else {
            if (backCameraId != "") {
                // Create the CameraCapturer with the back camera
                val cameraCapturer = CameraCapturer(applicationContext, backCameraId)

                // Create a video track
                localVideoTrack = LocalVideoTrack.create(applicationContext, enable, cameraCapturer)!!

                // Back camera should not be mirrored
                localVideoView.mirror = false

                // Render a local video track to preview camera
                localVideoTrack.addSink(localVideoView)
                localVideoTrack.addSink(localVideoViewIsDialing)
            }
        }
    }
    //************************************************** END INITIAL SET UP **************************************************

    //************************************************** ROTATE CAMERA **************************************************
    // The function to rotate camera and set things up again
    private fun rotateCameraAndReset () {
        if (isUsingCamera == "front") {
            if (frontCameraId != "") {
                // Create the CameraCapturer with the front camera
                val cameraCapturer = CameraCapturer(applicationContext, frontCameraId)

                // Create a video track
                localVideoTrack =
                    LocalVideoTrack.create(applicationContext, enable, cameraCapturer)!!

                // Front camera should be mirrored
                localVideoView.mirror = true

                // Render a local video track to preview camera
                localVideoTrack.addSink(localVideoView)
                localVideoTrack.addSink(localVideoViewIsDialing)

                userRepository.getInfoOfCurrentUser { userObject ->
                    connectToAChatRoom(chatRoomName, userObject.getId())
                }
            }
        } else {
            if (backCameraId != "") {
                // Create the CameraCapturer with the back camera
                val cameraCapturer = CameraCapturer(applicationContext, backCameraId)

                // Create a video track
                localVideoTrack = LocalVideoTrack.create(applicationContext, enable, cameraCapturer)!!

                // Back camera should not be mirrored
                localVideoView.mirror = false

                // Render a local video track to preview camera
                localVideoTrack.addSink(localVideoView)
                localVideoTrack.addSink(localVideoViewIsDialing)

                userRepository.getInfoOfCurrentUser { userObject ->
                    connectToAChatRoom(chatRoomName, userObject.getId())
                }

                localVideoTrack.let {
                    val enable = !it.isEnabled
                    it.enable(enable)

                    val icon = if (enable)
                        R.drawable.ic_baseline_videocam_24
                    else
                        R.drawable.ic_baseline_videocam_off_24
                    cameraSwitchButton.setImageDrawable(ContextCompat.getDrawable(
                        applicationContext, icon))
                }
            }
        }
    }
    //************************************************** ROTATE CAMERA **************************************************

    //************************************************** MIC AND VIDEO SWITCH **************************************************
    // The function to switch mic off and on
    private fun micSwitch () {
        localAudioTrack.let {
            val enable = !it.isEnabled
            it.enable(enable)
            val icon = if (enable)
                R.drawable.ic_baseline_mic_24
            else
                R.drawable.ic_baseline_mic_off_24
            micSwitchButton.setImageDrawable(ContextCompat.getDrawable(
                applicationContext, icon))
        }
    }

    // The function to switch camera off and on
    private fun cameraSwitch () {
        localVideoTrack.let {
            val enable = !it.isEnabled
            it.enable(enable)
            val icon = if (enable)
                R.drawable.ic_baseline_videocam_24
            else
                R.drawable.ic_baseline_videocam_off_24
            cameraSwitchButton.setImageDrawable(ContextCompat.getDrawable(
                applicationContext, icon))
        }
    }
    //************************************************** END MIC AND VIDEO SWITCH **************************************************
}