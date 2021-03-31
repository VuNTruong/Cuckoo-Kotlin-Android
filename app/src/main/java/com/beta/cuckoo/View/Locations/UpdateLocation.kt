package com.beta.cuckoo.View.Locations

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.LocationRepositories.LocationRepository
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_update_location.*
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UpdateLocation : AppCompatActivity(), PermissionsListener {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Location repository
    private lateinit var locationRepository: LocationRepository

    // Current location of the user (maybe used later on in the app)
    private lateinit var userCurrentLocation: LatLng

    // Style of the map (to be initiated later in the app)
    private lateinit var style: Style

    // Map object (to be initiated later in the app)
    private lateinit var mapbox: MapboxMap

    // Permission to access current location of the user
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    // The location engine which will be used to locate user (to be initiated later in the app)
    private lateinit var locationEngine : LocationEngine

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate the location repository
        locationRepository = LocationRepository(executorService, applicationContext)

        // Get mapbox instance
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_update_location)

        // Set on click listener for the back button
        backButtonUpdateLocation.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Initiate the last updated location map view
        mapViewLastUpdatedLocation.onCreate(savedInstanceState)
        mapViewLastUpdatedLocation.getMapAsync{mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Call the function to get last updated location of the user
                getInfoOfCurrentUserAndLastUpdatedLocation(mapboxMap)
            }
        }

        // Initiate the update location map view
        mapViewUpdateLocation.onCreate(savedInstanceState)
        mapViewUpdateLocation.getMapAsync { mapboxMap ->
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                // Set the mapbox property of the class to be the update location map view
                mapbox = mapboxMap

                // The current location call back object
                val currentLocationCallback =
                    LocationChangeListeningActivityLocationCallback(
                        this
                    )

                // Call the function to start getting current location of the current user
                initLocationEngine(currentLocationCallback)

                // Call the function to show user's current location on the map
                enableLocationComponent(it, mapboxMap)
            }
        }

        // Set on click listener for the update location button
        updateLocationButton.setOnClickListener {
            // Call the function to update location of the user
            updateUserLocation(whatAreYouDoingEditText.text.toString(), userCurrentLocation)
        }
    }
    //*********************************** OBTAIN USER'S LOCATION SEQUENCE ***********************************
    // The function to ask for user's permission to use location


    //*********************************** END OBTAIN USER'S LOCATION SEQUENCE ***********************************

    //*********************************** LOAD USER'S LAST UPDATED LOCATION SEQUENCE ***********************************
    /*
    In this sequence, we will do 3 things
    1. Get info of the current user
    2. Load last updated location of the user
    3. Load that location into a map
     */

    // The function to get info of the current user
    private fun getInfoOfCurrentUserAndLastUpdatedLocation (mapbox: MapboxMap) {
        // Call the function to get last updated location of the currently logged in user
        locationRepository.getLastUpdatedLocationOfCurrentUser { lastUpdatedLocation, locationDescription ->
            // Call the function to pin last updated location of the user on the map
            pinUser(lastUpdatedLocation, locationDescription, mapbox)
        }
    }

    // The function to add marker on map for the selected user
    // The function to pin the users
    private fun pinUser (location: LatLng, title: String, mapbox: MapboxMap) {
        // Add pin at the user location
        mapbox.addMarker(
            MarkerOptions()
                .position(location)
                .title(title))

        // Zoom into user location
        val position = CameraPosition.Builder()
            .target(location)
            .zoom(DEFAULT_ZOOM)
            .build()
        mapbox.animateCamera(CameraUpdateFactory.newCameraPosition(position),
            millisecondSpeed
        )
    }
    //*********************************** END LOAD USER'S LAST UPDATED LOCATION SEQUENCE ***********************************

    //*********************************** UPDATE USER'S LOCATION SEQUENCE ***********************************
    /*
    In this sequence, we will do 3 things
    1. Get user's permission to access current location
    2. Get current user's info
    3/ Update user's location based on the location we got earlier
     */

    //--------------------------------- THE SEQUENCE OF GETTING USER'S CURRENT LOCATION ---------------------------------
    // The function to ask for user permission to use the location
    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        // Do something here later :))
        Toast.makeText(applicationContext, "We need  your permission to access your current location", Toast.LENGTH_LONG).show()
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

    // The function to get user's current location and show it on the map
    private fun enableLocationComponent(loadedMapStyle: Style, mapboxMap: MapboxMap) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.colorPrimary))
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
                        this@UpdateLocation.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@UpdateLocation.applicationContext,
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

                // Zoom in the user's current location
                zoomWhileTracking(DEFAULT_ZOOM, DEFAULT_ANIMATION_DURATION.toLong())
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    // The function to start obtaining user's current location and get the latitude as well as longitude
    private fun initLocationEngine(callback: LocationChangeListeningActivityLocationCallback) {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        val request =
            LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
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
        locationEngine.requestLocationUpdates(request, callback, mainLooper)
        locationEngine.getLastLocation(callback)
    }

    // The function to set current location for the user after it is obtained from the private class below this
    private fun setUserCurrentLocation (currentLocation: LatLng) {
        userCurrentLocation = currentLocation
    }

    // Private class for getting current location of the user
    private class LocationChangeListeningActivityLocationCallback (activity: UpdateLocation) :
        LocationEngineCallback<LocationEngineResult> {
        private var activityWeakReference: WeakReference<UpdateLocation> = WeakReference(activity)

        override fun onSuccess(result: LocationEngineResult?) {
            // Location change listener
            val activity : UpdateLocation = activityWeakReference.get()!!

            // Get latest location of the user
            val location = result!!.lastLocation!!

            // Get latitude and longitude of the user
            val lat = location.latitude
            val long = location.longitude

            // Call the function to set current location for the user
            activity.setUserCurrentLocation(LatLng(lat, long))
        }

        override fun onFailure(exception: Exception) {
            // Handle error
            // Report the error
            print(exception.stackTrace)
        }
    }
    //--------------------------------- END SEQUENCE OF GETTING USER'S CURRENT LOCATION ---------------------------------
    // The function to update user's location
    fun updateUserLocation (whatAreYouDoing: String, location: LatLng) {
        // Call the function to update location of the currently logged in user
        locationRepository.updateLocation(whatAreYouDoing, location)
    }
    //--------------------------------- END UPDATE USER'S LOCATION SEQUENCE ---------------------------------

    //*********************************** UPDATE USER'S LOCATION SEQUENCE ***********************************

    // Several objects to be used by the map
    companion object {
        private const val DEFAULT_ZOOM = 20.0
        private const val DEFAULT_ANIMATION_DURATION = 2
        private const val millisecondSpeed = 2
        private const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        private const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    }
}