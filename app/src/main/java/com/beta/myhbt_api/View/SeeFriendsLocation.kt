package com.beta.myhbt_api.View

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetFollowingService
import com.beta.myhbt_api.Controller.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
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

class SeeFriendsLocation : AppCompatActivity(), PermissionsListener {
    private lateinit var style: Style
    private lateinit var mapbox: MapboxMap
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_see_friends_location)

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

                    // Call the function to load list of friends of the current user
                    getListOfFollowingOfUserAndLocation(currentUserId, mapbox)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to load list of following for the current user
    fun getListOfFollowingOfUserAndLocation (userId: String, mapbox: MapboxMap) {
        // Create the service for getting number of followings
        val getArrayOfFollowingService: GetFollowingService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetFollowingService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getArrayOfFollowingService.getFollowings(userId)

        // Perform the call
        call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("There seem to be an error ${t.stackTrace}")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is data
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data of the response
                    val data = responseBody["data"] as Map<String, Any>

                    // Get list of following
                    val listOfFollowings = data["documents"] as ArrayList<Map<String, Any>>

                    // Loop through the list of followings to obtain list of user id of users to whom the current user is following
                    for (following in listOfFollowings) {
                        // Get the following id and add it to the array
                        val followingUserId = following["following"] as String

                        // Call the function to pin users to whom the current user is following on the map
                        loadUserInfoBasedOnId(followingUserId, mapbox)
                    }
                }
            }
        })
    }

    // The function to get info of a user based on id
    fun loadUserInfoBasedOnId (userId: String, mapbox: MapboxMap) {
        // Create the get user info base on id service
        val getUserInfoBasedOnUserIdService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(GetUserInfoBasedOnIdService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserInfoBasedOnUserIdService.getUserInfoBasedOnId(userId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is no error
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user info from the data
                    val userInfo = (data["documents"] as List<Map<String, Any>>)[0]

                    // Get full name of the user
                    val userFullName = userInfo["fullName"] as String

                    //------------- Get location of the user -------------
                    // Get last updated location of the current user
                    val locationObject = userInfo["location"] as Map<String, Any>
                    val coordinatesArray = locationObject["coordinates"] as ArrayList<Double>

                    // Get description of the user location
                    val locationDescription = locationObject["description"] as String

                    // Get the latitude
                    val latitude = coordinatesArray[1]

                    // Get the longitude
                    val longitude = coordinatesArray[0]

                    // Create the location object for the last updated location of the current user
                    val center = LatLng(latitude, longitude)
                    //------------- End get location of the user -------------

                    // Call the function to pin the user on the map
                    pinUser(center, "${userFullName}: $locationDescription", mapbox)
                }
            }
        })
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