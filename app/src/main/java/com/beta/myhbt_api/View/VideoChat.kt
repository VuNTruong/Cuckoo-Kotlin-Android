package com.beta.myhbt_api.View

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beta.myhbt_api.BackgroundServices
import com.beta.myhbt_api.R
import com.beta.myhbt_api.Utils.SimpleSdpObserver
import com.google.gson.Gson
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_video_chat.*
import org.json.JSONObject
import org.webrtc.*
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.*

class VideoChat : AppCompatActivity(), PermissionsListener {
    private val TAG = "CompleteActivity"
    private val RC_CALL = 111
    val VIDEO_TRACK_ID = "ARDAMSv0"
    val VIDEO_RESOLUTION_WIDTH = 1280
    val VIDEO_RESOLUTION_HEIGHT = 720
    val FPS = 30

    // Chat room id between the 2 users
    private var chatRoomId = "aabc"

    // The variable to keep track of if the video chat is start or not
    private var isStarted = false

    // The variable which will keep track of if current user is the initiator of the call or not
    private var isInitiator = false

    // The variable which will keep track of if the video channel is ready or not
    private var isChannelReady = false

    // These objects are used for socket.io
    private val gson = Gson()

    // Permission to do video call
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    private lateinit var rootBase: EglBase

    // Peer connection factory
    private lateinit var factory: PeerConnectionFactory

    // Peer connection object
    private lateinit var peerConnection: PeerConnection

    // Video tracker
    private lateinit var videoTrackFromCamera: VideoTrack

    // Audio constraints
    private lateinit var audioConstraints: MediaConstraints

    // Audio source
    private lateinit var audioSource: AudioSource

    // Local audio track
    private lateinit var localAudioTrack: AudioTrack

    private lateinit var mSocket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat)

        val intentService = Intent(this, BackgroundServices::class.java)
        startService(intentService)

        // Call the function to ask for permission
        checkPermission(android.Manifest.permission.CAMERA, 1)

        // Initialize the peer connection factory
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions()
        )
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory()

        // Initialize surface views and set up environment
        initiateSurfaceViews()

        // Set up peer connection
        peerConnection = createPeerConnection(factory)

        // Call the function to set up socket io
        setUpSocketIO()

        // Call the function to start streaming video
        startSteamingVideo()
    }

    //********************************* DO THINGS WITH SOCKET IO SEQUENCE *********************************
    // The function to set up socket io
    private fun setUpSocketIO () {
        // This address is to connect with the server
        mSocket = IO.socket("http://10.0.2.2:3000")
        //mSocket = IO.socket("https://myhbt-api.herokuapp.com")
        //mSocket = IO.socket("http://localhost:3000")
        mSocket.connect()

        // When user is connected, emit event to bring user into the video chat room
        mSocket.on(Socket.EVENT_CONNECT, onEventConnected)

        // Listen to event of when stream data is received
        mSocket.on("streamDataOut", onStreamDataReceived)

        // Listen to event of when room is created
        mSocket.on("created", onRoomCreated)

        // Listen to event of when this user created and joined the room
        mSocket.on("join", onJoin)

        // Listen to event of when this user joined in the created room
        mSocket.on("joined", onJoined)

        // Listen to event of when media from other user is received
        mSocket.on("got user media", onReceivedMedia)
    }

    // Callback functions to do things when corresponding events from server is received
    private var onStreamDataReceived = Emitter.Listener {
        // Stream data object from the server
        val streamDataObject = it[0]

        // Stream data object from the server in map data structure
        val map = HashMap<String, Any>()

        val jObject = JSONObject(streamDataObject.toString())
        val keys: Iterator<*> = jObject.keys()

        while (keys.hasNext()) {
            val key = keys.next() as String
            val value: Any = jObject.getString(key)
            map[key] = value
        }

        // Based on which type of stream data to do the right thing
        when {
            map["type"] as String == "offer" -> {
                peerConnection.setRemoteDescription(SimpleSdpObserver(), SessionDescription(SessionDescription.Type.OFFER, map["sdp"] as String))
                doAnswer()
            }
            map["type"] as String == "answer" -> {
                peerConnection.setRemoteDescription(SimpleSdpObserver(), SessionDescription(SessionDescription.Type.ANSWER, map["sdp"] as String))
            }
            map["type"] as String == "candidate" -> {
                val candidate = IceCandidate(map["id"] as String, map["label"] as Int, map["candidate"] as String)
                peerConnection.addIceCandidate(candidate)
            }
        }
    }

    private var onEventConnected = Emitter.Listener {
        // Emit event to the server to let user into the room
        mSocket.emit("create or join", gson.toJson(hashMapOf(
            "chatRoomId" to chatRoomId
        )))
    }

    private var onRoomCreated = Emitter.Listener {
        isInitiator = true
    }

    private var onJoin = Emitter.Listener {
        isChannelReady = true
    }

    private var onJoined = Emitter.Listener {
        isChannelReady = true
    }

    private var onReceivedMedia = Emitter.Listener {
        maybeStart()
    }
    //********************************* END DO THINGS WITH SOCKET IO SEQUENCE *********************************

    //********************************* TAKE USER PERMISSION SEQUENCE *********************************
    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission)
            == PackageManager.PERMISSION_DENIED
        ) {

            // Requesting the permission
            ActivityCompat.requestPermissions(
                this, arrayOf(permission),
                requestCode
            )
        } else {
            Toast.makeText(
                this,
                "Permission already granted",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    // The function to ask for user permission to use the location
    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        // Do something here later :))
        Toast.makeText(
            applicationContext,
            "We need  your permission to do video chat",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            // Call the function to set up video call
            initiateSurfaceViews()
        } else {
            // Don't do anything here
            return
        }
    }
    //********************************* END TAKE USER PERMISSION SEQUENCE *********************************

    //********************************* ACCESS CAMERA DATA SEQUENCE *********************************
    // The function to initiate the surface view (show things from camera)
    private fun initiateSurfaceViews () {
        rootBase = EglBase.create()

        surface_view.init(rootBase.eglBaseContext, null)
        surface_view.setEnableHardwareScaler(true)
        surface_view.setMirror(true)

        surface_view2.init(rootBase.eglBaseContext, null)
        surface_view2.setEnableHardwareScaler(true)
        surface_view2.setMirror(true)

        createVideoTrackFromCameraAndShow()
    }

    // The function to create video track from camera and show
    private fun createVideoTrackFromCameraAndShow () {
        audioConstraints = MediaConstraints()

        val videoCapturer = createVideoCapturer()
        val surfaceCaptureHelper = SurfaceTextureHelper.create(
            "CaptureThread",
            rootBase.eglBaseContext
        )
        val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
        videoCapturer.initialize(surfaceCaptureHelper, this, videoSource.capturerObserver)

        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS)

        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource)
        videoTrackFromCamera.setEnabled(true)
        videoTrackFromCamera.addSink(surface_view)

        // Create audio source
        audioSource = factory.createAudioSource(audioConstraints)
        localAudioTrack = factory.createAudioTrack("101", audioSource)
    }

    // The function to create video capturer
    private fun createVideoCapturer (): VideoCapturer {
        return if (useCamera2()) {
            val videoCapturer = createCameraCapturer(Camera2Enumerator(this))
            videoCapturer!!
        } else {
            val videoCapturer = createCameraCapturer(Camera1Enumerator(true))
            videoCapturer!!
        }
    }

    // The function to create camera capturer
    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames

        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        return null
    }

    private fun useCamera2(): Boolean {
        return Camera2Enumerator.isSupported(this)
    }
    //********************************* END ACCESS CAMERA DATA SEQUENCE *********************************

    //********************************* START AND RECEIVE CALL SEQUENCE *********************************
    // The function for the user to start the call
    private fun doCall () {
        // Media constraints
        val sdpMediaConstraints = MediaConstraints()

        // Add some info to the media constraints
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveAudio",
                "true"
            )
        )
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo",
                "true"
            )
        )
        // Create offer
        peerConnection.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                peerConnection.setLocalDescription(SimpleSdpObserver(), sessionDescription)

                // Emit stream data to the server
                mSocket.emit(
                    "streamDataIn", gson.toJson(
                        hashMapOf(
                            "type" to "offer",
                            "sdp" to sessionDescription!!.description,
                            "chatRoomId" to chatRoomId
                        )
                    )
                )
            }
        }, sdpMediaConstraints)
    }

    // The function for the user to receive the call
    private fun doAnswer () {
        // Use the peer connection object to start answering
        peerConnection.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                peerConnection.setLocalDescription(SimpleSdpObserver(), sessionDescription)

                // Emit stream data to the server
                mSocket.emit(
                    "streamDataIn", gson.toJson(
                        hashMapOf(
                            "type" to "answer",
                            "sdp" to sessionDescription!!.description,
                            "chatRoomId" to chatRoomId
                        )
                    )
                )
            }
        }, MediaConstraints())
    }
    //********************************* END START AND RECEIVE CALL SEQUENCE *********************************

    //********************************* CREATE PEER CONNECTION SEQUENCE *********************************
    // The function to create peer connection
    private fun createPeerConnection(factory: PeerConnectionFactory): PeerConnection {
        val iceServers = ArrayList<PeerConnection.IceServer>()
        iceServers.add(PeerConnection.IceServer("stun:stun.l.google.com:19302"))

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        val pcConstraints = MediaConstraints()

        val pcObserver: Observer = object : Observer {
            override fun onSignalingChange(signalingState: SignalingState) {
                Log.d(TAG, "onSignalingChange: ")
            }

            override fun onIceConnectionChange(iceConnectionState: IceConnectionState) {
                Log.d(TAG, "onIceConnectionChange: ")
            }

            override fun onIceConnectionReceivingChange(b: Boolean) {
                Log.d(TAG, "onIceConnectionReceivingChange: ")
            }

            override fun onIceGatheringChange(iceGatheringState: IceGatheringState) {
                Log.d(TAG, "onIceGatheringChange: ")
            }

            override fun onIceCandidate(iceCandidate: IceCandidate) {
                Log.d(TAG, "onIceCandidate: ")
                // Emit stream data to the server
                BackgroundServices.mSocket.emit("streamDataOut", gson.toJson(hashMapOf(
                    "type" to "candicate",
                    "label" to iceCandidate.sdpMLineIndex,
                    "id" to iceCandidate.sdpMid,
                    "candidate" to iceCandidate.sdp
                )))
            }

            override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
                Log.d(TAG, "onIceCandidatesRemoved: ")
            }

            override fun onAddStream(mediaStream: MediaStream) {
                Log.d(TAG, "onAddStream: " + mediaStream.videoTracks.size)
                val remoteVideoTrack = mediaStream.videoTracks[0]
                val remoteAudioTrack = mediaStream.audioTracks[0]
                remoteAudioTrack.setEnabled(true)
                remoteVideoTrack.setEnabled(true)
                remoteVideoTrack.addSink(surface_view2)
            }

            override fun onRemoveStream(mediaStream: MediaStream) {
                Log.d(TAG, "onRemoveStream: ")
            }

            override fun onDataChannel(dataChannel: DataChannel) {
                Log.d(TAG, "onDataChannel: ")
            }

            override fun onRenegotiationNeeded() {
                Log.d(TAG, "onRenegotiationNeeded: ")
            }

            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
            }
        }

        // Return the peer connection
        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver)!!
    }
    //********************************* END CREATE PEER CONNECTION SEQUENCE *********************************

    //********************************* STREAMING VIDEO SEQUENCE *********************************
    // The function to start streaming video
    private fun startSteamingVideo () {
        val mediaStream = factory.createLocalMediaStream("ARDAMS")
        mediaStream.addTrack(videoTrackFromCamera)
        mediaStream.addTrack(localAudioTrack)
        peerConnection.addStream(mediaStream)

        // Emit event to let other users know that they got some media
        mSocket.emit("got user media", gson.toJson(hashMapOf(
            "chatRoomId" to chatRoomId
        )))
    }
    //********************************* END STREAMING VIDEO SEQUENCE *********************************

    //********************************* CHECK CALL STATUS SEQUENCE *********************************
    // The function to check call status
    private fun maybeStart () {
        if (!isStarted && isChannelReady) {
            isStarted = true
            if (isInitiator) {
                doCall()
            }
        }
    }
    //********************************* END CHECK CALL STATUS SEQUENCE *********************************
}