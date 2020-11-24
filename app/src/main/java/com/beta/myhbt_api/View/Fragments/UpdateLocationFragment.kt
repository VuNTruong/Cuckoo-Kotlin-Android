package com.beta.myhbt_api.View.Fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Controller.UpdateUserLocationService
import com.beta.myhbt_api.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_update_location.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateLocationFragment : Fragment() {
    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // User's permission to use location will be false initially
    private var locationPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_update_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = this.requireActivity().supportFragmentManager.findFragmentById(R.id.mapViewUpdateLocation) as? SupportMapFragment
        mapFragment!!.getMapAsync{googleMap ->
            // Call the function to obtain user's permission
            getLocationPermission()

            // Call the function to update the map's UI
            updateLocationUI(googleMap!!)

            // Call the function to obtain the user's location
            getDeviceLocation(googleMap)
        }

        // Get the SupportMapFragment and request notification when the last updated location map is ready to be used
        val mapFragmentLastUpdatedLocation = this.requireActivity().supportFragmentManager.findFragmentById(R.id.mapViewUpdateLocationLastKnownLocation) as? SupportMapFragment
        mapFragmentLastUpdatedLocation?.getMapAsync { googleMap ->
            // Call the function to set up last updated location for the current user
            getInfoOfCurrentUserAndLastUpdatedLocation(googleMap)
        }

        // Set up event listener for the update location button
        updateLocationButton.setOnClickListener {
            // Call the function to update user's location
            getInfoOfCurrentUserAndUpdateLocation(lastKnownLocation!!, whatAreYouDoingEditText.text.toString())
        }
    }

    //*********************************** OBTAIN USER'S LOCATION SEQUENCE ***********************************
    // The function to ask for user's permission to use location
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    // The function to handle actions based on user's permission
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
    }

    // The function to update the map UI
    private fun updateLocationUI(map: GoogleMap) {
        try {
            if (locationPermissionGranted) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    // The function to get current location of the device
    private fun getDeviceLocation(map: GoogleMap) {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this.requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            val lat = lastKnownLocation!!.latitude
                            val long = lastKnownLocation!!.longitude

                            print("Lat long")

                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    // Several objects to be used by the map
    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
    //*********************************** END OBTAIN USER'S LOCATION SEQUENCE ***********************************

    //*********************************** LOAD USER'S LAST UPDATED LOCATION SEQUENCE ***********************************
    /*
    In this sequence, we will do 3 things
    1. Get info of the current user
    2. Load last updated location of the user
    3. Load that location into a map
     */

    // The function to get info of the current user
    private fun getInfoOfCurrentUserAndLastUpdatedLocation (googleMap: GoogleMap) {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
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

                    // Call the function to set up pin for the last updated location of the current user
                    addMarker(center, locationDescription, googleMap)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to add marker on map for the selected user
    private fun addMarker(latlng: LatLng, title: String, gMap: GoogleMap) {
        val cameraPosition = CameraPosition.Builder().target(latlng).zoom(DEFAULT_ZOOM.toFloat()).build()
        gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        val markerOptions = MarkerOptions()

        markerOptions.position(latlng)
        markerOptions.title(title)
        gMap.addMarker(markerOptions)
        gMap.setOnInfoWindowClickListener { marker ->
            Toast.makeText(this.requireActivity(), marker.title, Toast.LENGTH_SHORT)
                .show()
        }
    }
    //*********************************** END LOAD USER'S LAST UPDATED LOCATION SEQUENCE ***********************************

    //*********************************** UPDATE USER'S LOCATION SEQUENCE ***********************************
    /*
    In this sequence, we will do 2 things
    1. Get current user's info
    2/ Update user's location based on the location we got earlier
     */

    // The function to get info of the current user
    private fun getInfoOfCurrentUserAndUpdateLocation (location: Location, whatAreYouDoing: String) {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
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
    fun updateUserLocation (currentUserId: String, whatAreYouDoing: String, location: Location) {
        // Create the update user location service
        val updateUserLocationService: UpdateUserLocationService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
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
                    Toast.makeText(this@UpdateLocationFragment.requireActivity(), "Something is not right", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@UpdateLocationFragment.requireActivity(), "Updated", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    //*********************************** UPDATE USER'S LOCATION SEQUENCE ***********************************
}