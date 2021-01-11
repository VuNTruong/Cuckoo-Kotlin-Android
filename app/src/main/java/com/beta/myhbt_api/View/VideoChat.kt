package com.beta.myhbt_api.View

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beta.myhbt_api.R
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import kotlinx.android.synthetic.main.activity_video_chat.*
import org.webrtc.*
import java.util.jar.Manifest


class VideoChat : AppCompatActivity(), PermissionsListener {
    private val TAG = "CompleteActivity"
    private val RC_CALL = 111
    val VIDEO_TRACK_ID = "ARDAMSv0"
    val VIDEO_RESOLUTION_WIDTH = 1280
    val VIDEO_RESOLUTION_HEIGHT = 720
    val FPS = 30

    // Permission to do video call
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    private lateinit var rootBase: EglBase

    // Peer connection factory
    private lateinit var factory: PeerConnectionFactory

    // Video tracker
    private lateinit var videoTrackFromCamera: VideoTrack

    // Audio constraints
    private lateinit var audioConstraints: MediaConstraints

    // Audio source
    private lateinit var audioSource: AudioSource

    // Local audio track
    private lateinit var localAudioTrack: AudioTrack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat)

        checkPermission(android.Manifest.permission.CAMERA, 1)

        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions()
        )
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory()

        initiateSurfaceViews()
    }

    // Function to check and request permission.
    fun checkPermission(permission: String, requestCode: Int) {
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
        grantResults: IntArrayx
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
}