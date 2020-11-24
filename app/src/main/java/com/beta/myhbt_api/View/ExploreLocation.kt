package com.beta.myhbt_api.View

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beta.myhbt_api.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.activity_explore_location.*


class ExploreLocation : AppCompatActivity(), OnMapReadyCallback {
    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // User's permission to use location will be false initially
    private var locationPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore_location)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        /*
        val center = LatLng(-33.852, 151.211)
        val cameraPosition = CameraPosition.Builder().target(center).zoom(50.toFloat()).build()
        googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
         */
        //addMarker(center, "Sydney", googleMap)

        getLocationPermission()
        updateLocationUI(googleMap!!)
        getDeviceLocation(googleMap)
    }

    // The function to add marker on map for the selected user
    private fun addMarker(latlng: LatLng, title: String, gMap: GoogleMap) {
        val markerOptions = MarkerOptions()

        markerOptions.position(latlng)
        markerOptions.title(title)
        gMap.addMarker(markerOptions)
        gMap.setOnInfoWindowClickListener { marker ->
            Toast.makeText(applicationContext, marker.title, Toast.LENGTH_SHORT)
                .show()
        }
    }

    // The function to ask for user's permission to use location
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
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
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            val lat = lastKnownLocation!!.latitude
                            val long = lastKnownLocation!!.longitude

                            print("$lat $long")
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
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
}