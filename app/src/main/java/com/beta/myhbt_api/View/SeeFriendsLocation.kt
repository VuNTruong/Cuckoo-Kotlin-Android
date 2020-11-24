package com.beta.myhbt_api.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetFollowingService
import com.beta.myhbt_api.Controller.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SeeFriendsLocation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_friends_location)

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapViewSeeFriendsLocation) as? SupportMapFragment
        mapFragment!!.getMapAsync{googleMap ->
            // Call the function to load list of friends, then show locations
            getInfoOfCurrentUserAndFriendsLocation(googleMap)
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
    private fun getInfoOfCurrentUserAndFriendsLocation (googleMap: GoogleMap) {
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
                    getListOfFollowingOfUserAndLocation(currentUserId, googleMap)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to load list of following for the current user
    fun getListOfFollowingOfUserAndLocation (userId: String, googleMap: GoogleMap) {
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
                        loadUserInfoBasedOnId(followingUserId, googleMap)
                    }
                }
            }
        })
    }

    // The function to get info of a user based on id
    fun loadUserInfoBasedOnId (userId: String, googleMap: GoogleMap) {
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
                    addMarker(center, "${userFullName}: $locationDescription", googleMap)
                }
            }
        })
    }

    // The function to pin a user on a map
    private fun addMarker(latlng: LatLng, title: String, gMap: GoogleMap) {
        val cameraPosition = CameraPosition.Builder().target(latlng).zoom(DEFAULT_ZOOM.toFloat()).build()
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        val markerOptions = MarkerOptions()

        markerOptions.position(latlng)
        markerOptions.title(title)
        gMap.addMarker(markerOptions)
        gMap.setOnInfoWindowClickListener { marker ->
            Toast.makeText(this, marker.title, Toast.LENGTH_SHORT)
                .show()
        }
    }
    //**************************************** END LOAD LIST OF FRIENDS AND LOCATION SEQUENCE ****************************************

    // Several objects to be used by the map
    companion object {
        private const val DEFAULT_ZOOM = 15
    }
}