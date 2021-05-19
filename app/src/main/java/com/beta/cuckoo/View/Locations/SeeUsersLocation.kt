package com.beta.cuckoo.View.Locations

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beta.cuckoo.Network.User.GetCurrentlyLoggedInUserInfoService
import com.beta.cuckoo.Network.Follows.GetFollowingService
import com.beta.cuckoo.Network.User.GetUserInfoBasedOnIdService
import com.beta.cuckoo.Network.RetrofitClientInstance
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.LocationRepositories.LocationRepository
import com.beta.cuckoo.Repository.UserRepositories.FollowRepository
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_see_friends_location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SeeUsersLocation : AppCompatActivity(), PermissionsListener {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    private lateinit var style: Style
    private lateinit var mapbox: MapboxMap
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    // Follow repository
    private lateinit var followRepository: FollowRepository

    // Location repository
    private lateinit var locationRepository: LocationRepository

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the action bar
        supportActionBar!!.hide()

        // Get mapbox instance
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_see_friends_location)

        // Instantiate follow repository
        followRepository = FollowRepository(executorService, applicationContext)

        // Instantiate location repository
        locationRepository = LocationRepository(executorService, applicationContext)

        // Set on click listener for the back button
        backButtonSeeFriendsLocation.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        findFriendsMapView?.onCreate(savedInstanceState)
        findFriendsMapView?.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                getInfoOfCurrentUserAndFriendsLocation(mapboxMap)

                enableLocationComponent(it, mapboxMap)
                style = it
                mapbox = mapboxMap
            }
        }
    }

    //**************************************** LOAD LIST OF FRIENDS AND LOCATION SEQUENCE ****************************************
    /*
    In this sequence, we will do 4 things
    1. Get info of the current user
    2. Get list of friends of the current user
    3. Get their location
    4. Pin them on the map
     */

    // The function to get info of the current user
    private fun getInfoOfCurrentUserAndFriendsLocation (mapbox: MapboxMap) {
        // Call the function to get list of followings of the currently logged in user
        followRepository.getLisOfFollowingsOfCurrentUser { arrayOfUserId ->
            // Loop through the list of followings to obtain list of user id of users to whom the current user is following
            for (followingUserId in arrayOfUserId) {
                // Call the function to pin users to whom the current user is following on the map
                loadUserInfoBasedOnId(followingUserId, mapbox)
            }
        }
    }

    // The function to get info of a user based on id
    private fun loadUserInfoBasedOnId (userId: String, mapbox: MapboxMap) {
        // Call the function to get location info of the user with specified user id and pin that user on the map
        locationRepository.getLocationInfoOfUserBasedOnId(userId) {userFullName, locationDescription, location ->
            // Call the function to pin the user on the map
            pinUser(location, "${userFullName}: $locationDescription", mapbox)
        }
    }

    // The function to pin the users
    private fun pinUser (location: LatLng, title: String, mapbox: MapboxMap) {
        // Add pin at the user location
        mapbox.addMarker(
            MarkerOptions()
                .position(location)
                .title(title))
    }
    //**************************************** END LOAD LIST OF FRIENDS AND LOCATION SEQUENCE ****************************************

    //**************************************** LOAD USER CURRENT LOCATION SEQUENCE ****************************************
    /*
    In this sequence, we will do 3 things
    1. Ask for location permission of the user
    2. Reference the database and get current location of the user
    3. Pin the user location on the map
     */

    // The function to ask for user permission to use the location
    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        // Do something here later :))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            // User grant permission to use the location, call the function to get user location and pin it on the map
            enableLocationComponent(style, mapbox)
        } else {
            // Don't do anything here
            return
        }
    }

    // The function to get user's current location
    private fun enableLocationComponent(loadedMapStyle: Style, mapboxMap: MapboxMap) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.mapbox_blue))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap.locationComponent.apply {
                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                if (ActivityCompat.checkSelfPermission(
                        this@SeeUsersLocation,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@SeeUsersLocation,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS

                zoomWhileTracking(20.0, 2)
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }
    //**************************************** END LOAD USER CURRENT LOCATION SEQUENCE ****************************************

    // Several objects to be used by the map
    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val millisecondSpeed = 2
        private const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        private const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    }
}