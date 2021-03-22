package com.beta.cuckoo.View.VideoChat

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.VideoChatRepository.VideoChatRepository
import com.twilio.video.*
import kotlinx.android.synthetic.main.activity_video_chat.*
import kotlinx.android.synthetic.main.profile_page_item.*
import tvi.webrtc.Camera1Enumerator
import tvi.webrtc.Camera2Capturer
import tvi.webrtc.Camera2Enumerator
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.jar.Manifest

class VideoChat : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Video chat repository
    private lateinit var videoChatRepository: VideoChatRepository

    // Chat room name
    private var chatRoomName = "chat-room-go"

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
    private var isUsingCamera = "front"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat)

        // Instantiate video chat repository
        videoChatRepository = VideoChatRepository(executorService, applicationContext)

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

        // Set up on click listener for the connect button
        connectButton.setOnClickListener {
            // Call the function to start connecting
            connectToAChatRoom(chatRoomName, "vntruong1")
        }

        // Set up on click listener for the end button
        endButton.setOnClickListener {
            // Call the function to end the room
            endRoom(chatRoomName)
        }
    }

    //************************************************** VIDEO CHATTING TOOLS **************************************************
    // The function to switch camera
    private fun switchCamera () {
        localVideoTrack.release()

        // Check to see which one is being used and switch it
        if (isUsingCamera == "front") {
            isUsingCamera = "back"
        } else if (isUsingCamera == "back") {
            isUsingCamera = "front"
        }

        // Call the function to set up camera again
        setUpCameraCapture()
    }

    // The function to connect to a chat room
    private fun connectToAChatRoom (roomName: String, userId: String) {
        // Call the function to get access token for the user
        videoChatRepository.getAccessTokenIntoVideoChatRoom(roomName, userId) {accessToken ->
            // Call the function to create the video chat room
            videoChatRepository.createRoom(roomName) {isExisted, _ ->
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
    }

    // The function to end the video chat room
    private fun endRoom (roomName: String) {
        // Call the function to end the room
        videoChatRepository.deleteVideoRoomName(roomName) {isDeleted ->
            if (isDeleted) {
                // Finish the activity
                this.finish()
            }
        }
    }
    //************************************************** END VIDEO CHATTING TOOLS **************************************************

    //************************************************** LISTENERS **************************************************
    // Room listener
    private val roomListener = object : Room.Listener {
        override fun onConnected(room: Room) {
            // When a user join a room, get info of other user that is already inside room
            // and display their media
            room.remoteParticipants.firstOrNull()?.let { addRemoteParticipant(it) }

            Toast.makeText(applicationContext, "Connected", Toast.LENGTH_SHORT).show()
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

        override fun onVideoTrackSubscribed(
            remoteParticipant: RemoteParticipant,
            remoteVideoTrackPublication: RemoteVideoTrackPublication,
            remoteVideoTrack: RemoteVideoTrack
        ) {
            // To be implemented
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
            }
        }
    }
    //************************************************** END INITIAL SET UP **************************************************
}