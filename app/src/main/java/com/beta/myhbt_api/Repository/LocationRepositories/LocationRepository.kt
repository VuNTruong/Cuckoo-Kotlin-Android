package com.beta.myhbt_api.Repository.LocationRepositories

import android.content.Context
import android.widget.Toast
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Controller.User.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.User.UpdateUserLocationService
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.mapbox.mapboxsdk.geometry.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class LocationRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The user repository
    private val userRepository: UserRepository = UserRepository(executor, context)

    // The function to get last updated location of the currently logged in user
    fun getLastUpdatedLocationOfCurrentUser (callback: (lastUpdatedLocation: LatLng, locationDescription: String) -> Unit) {
        executor.execute {
            // Create the get current user info service
            val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
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

                        //---------------- Get last updated location of the user ----------------
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

                        // Call the callback function to return last updated location of the current user
                        callback(center, locationDescription)
                        //---------------- End get last updated location of the user ----------------
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to update location of the currently logged in user
    fun updateLocation (whatAreYouDoing: String, location: LatLng) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the update user location service
                val updateUserLocationService: UpdateUserLocationService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
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
                val call: Call<Any> = updateUserLocationService.updateUserLocation(requestBody, userObject.getId())

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
                            Toast.makeText(context, "Something is not right", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
        }
    }
}