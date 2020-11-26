package com.beta.myhbt_api.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Controller.UpdateUserLocationService
import com.beta.myhbt_api.R
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

class UpdateLocation : AppCompatActivity(), PermissionsListener {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_update_location)

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
                val currentLocationCallback = LocationChangeListeningActivityLocationCallback(this)

                // Call the function to start getting current location of the current user
                initLocationEngine(currentLocationCallback)

                // Call the function to show user's current location on the map
                enableLocationComponent(it, mapboxMap)
            }
        }

        // Set on click listener for the update location button
        updateLocationButton.setOnClickListener {
            // Call the function to update location of the user
            getInfoOfCurrentUserAndUpdateLocation(userCurrentLocation, whatAreYouDoingEditText.text.toString())
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
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(this)!!.create(
            GetCurrentlyLoggedInUserInfoService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getCurrentlyLoggedInUserInfoService.getCurrentUserInfo()

        // Perform the call
        call.enqueue(object: Callback<Any> {
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

                    // Get last updated location of the current user
                    val locationObject = data["location"] as Map<String, Any>
                    val coordinatesArray = locationObject["coordinates"] as ArrayList<Double>

                    // Get description of the user location
                    val locationDescription = locationObject["description"] as String

                    // Get the latitude
                    val latitude = coordinatesArray[1]

                    // Get the longitude
                    val longitude = coordinatesArray[0]

                    // Create the location object for the last updated location of the current user
                    val center = LatLng(latitude, longitude)

                    // Call the function to pin last updated location of the user on the map
                    pinUser(center, locationDescription, mapbox)
                } else {
                    print("Something is not right")
                }
            }
        })
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
        mapbox.animateCamera(CameraUpdateFactory.newCameraPosition(position), millisecondSpeed)
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

    //--------------------------------- UPDATE USER'S LOCATION SEQUENCE ---------------------------------
    // The function to get info of the current user
    private fun getInfoOfCurrentUserAndUpdateLocation (location: LatLng, whatAreYouDoing: String) {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(this)!!.create(
            GetCurrentlyLoggedInUserInfoService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getCurrentlyLoggedInUserInfoService.getCurrentUserInfo()

        // Perform the call
        call.enqueue(object: Callback<Any> {
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

                    // Get id of the current user
                    val currentUserId = data["_id"] as String

                    // Cal the function to update user location
                    updateUserLocation(currentUserId, whatAreYouDoing, location)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to update user's location
    fun updateUserLocation (currentUserId: String, whatAreYouDoing: String, location: LatLng) {
        // Create the update user location service
        val updateUserLocationService: UpdateUserLocationService = RetrofitClientInstance.getRetrofitInstance(this)!!.create(
            UpdateUserLocationService::class.java)

        // Body of the JSON request
        val requestBody = hashMapOf(
            "location" to hashMapOf(
                "description" to whatAreYouDoing,
                "type" to "Point",
                "coordinates" to arrayOf(location.longitude, location.latitude)
            )
        )

        // Create the call object in order to perform the call
        val call: Call<Any> = updateUserLocationService.updateUserLocation(requestBody, currentUserId)

        // Perform the API call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is null, it means that the user may didn't enter the correct email or password
                if (response.body() == null) {
                    // Show the user that the login was not successful
                    Toast.makeText(applicationContext, "Something is not right", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Updated", Toast.LENGTH_SHORT).show()
                }
            }
        })
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