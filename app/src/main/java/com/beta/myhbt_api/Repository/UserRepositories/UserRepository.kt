package com.beta.myhbt_api.Repository.UserRepositories

import android.content.Context
import android.location.Location
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Controller.Follows.GeteFollowerService
import com.beta.myhbt_api.Controller.User.*
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.Repository.LocationRepositories.LocationRepository
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterSearchFriend
import com.google.gson.Gson
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.fragment_search_friends.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class UserRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The location repository
    private val locationRepository: LocationRepository = LocationRepository(executor, context)

    // In order to prevent us from encountering the class cast exception, we need to do the following
    // Create the GSON object
    val gs = Gson()

    // The function to get info of the currently logged in user
    fun getInfoOfCurrentUser (callback: (userObject: User) -> Unit) {
        // Do works in the background
        executor.execute{
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

                        // Convert the data object which is currently a linked tree map into a JSON string
                        val js = gs.toJson(data)

                        // Convert the JSOn string back into User class
                        val userModel = gs.fromJson<User>(js, User::class.java)

                        // Define what to be returned in the callback function
                        callback(userModel)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to get info of the user based on specified user id
    fun getUserInfoBasedOnId (userId: String, callback: (userObject: User) -> Unit) {
        executor.execute {
            // Create the get user info based on id service
            val getUserInfoBasedOnIdService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetUserInfoBasedOnIdService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getUserInfoBasedOnIdService.getUserInfoBasedOnId(userId)

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

                        // Get user info from the received data
                        val userInfo = (data["documents"] as List<Map<String, Any>>)[0]

                        // Convert the user info data object which is currently a linked tree map into a JSON string
                        val js = gs.toJson(userInfo)

                        // Convert the JSOn string back into User class
                        val userModel = gs.fromJson<User>(js, User::class.java)

                        // Return the found user info
                        callback(userModel)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to validate login token of the current user
    fun checkToken (callback: (isValid: Boolean) -> Unit) {
        executor.execute {
            // Create the validate token service
            val validateTokenService: ValidateTokenPostService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                ValidateTokenPostService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = validateTokenService.validate()

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Let the view model know that token is valid via callback function
                        callback(true)
                    } else {
                        // Let the view model know that token is not valid via callback function
                        callback(false)
                    }
                }
            })
        }
    }

    // The function to log a user in
    fun login (email: String, password: String, callback: (loginSuccess: Boolean) -> Unit) {
        executor.execute {
            // Create the post service
            val postService: LoginPostDataService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                LoginPostDataService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = postService.login(email, password)

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is null, it means that the user may didn't enter the correct email or password
                    if (response.body() == null) {
                        // Let the view model know that login was not successful
                        callback(false)
                    } else {
                        // Let the view model know tat login was successful
                        callback(true)
                    }
                }
            })
        }
    }

    // The function to sign a user up
    fun signUp (fullName: String, email: String, password: String, confirmPassword: String, callback: (signUpSuccess: Boolean) -> Unit) {
        executor.execute{
            // Split full name into array
            val arrayOfFullName = fullName.split(" ").toTypedArray()

            // Create the sign up service
            val signUpService : SignUpService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                SignUpService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = signUpService.signUp(email, password,
                confirmPassword, arrayOfFullName[0], arrayOfFullName[1], arrayOfFullName[arrayOfFullName.size - 1], "user", "avatar", "cover")

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // Get body of the response
                    val body = response.body()

                    // If body is not null, sign up is success
                    if (body != null) {
                        // Let the view model know that sign up is success via callback function
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            })
        }
    }

    // The function to get last updated location of the currently logged in user
    fun getLocationOfCurrentUser (callback: (lastUpdatedLocation: LatLng) -> Unit) {
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

                    // Get the latitude
                    val latitude = coordinatesArray[1]

                    // Get the longitude
                    val longitude = coordinatesArray[0]

                    // Create the location object for the last updated location of the current user
                    val center = LatLng(latitude, longitude)

                    // Return last updated location of the currently logged in user via callback function
                    callback(center)
                    //---------------- End get last updated location of the user ----------------
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to search for user based on name
    fun searchUser (searchQuery: String, callback: (arrayOfUsers: ArrayList<User>) -> Unit) {
        // Do work in the background
        executor.execute {
            // Create the search user service
            val searchUserService : SearchUserService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                SearchUserService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = searchUserService.searchUser(searchQuery)

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

                        // Get data from the response body (array of found users)
                        val arrayOfFoundUsers = responseBody["data"] as ArrayList<User>

                        // Call the function to return array of users to the view model
                        callback(arrayOfFoundUsers)
                    }
                }
            })
        }
    }

    // The function to search for user around a specified last updated location of the current user
    fun searchUserAround (searchQuery: String, callback: (arrayOfUsers: ArrayList<User>) -> Unit) {
        // Do work in the background
        executor.execute {
            // Call the function to get last update location of the currently logged in user
            locationRepository.getLastUpdatedLocationOfCurrentUser { lastUpdatedLocation, _ ->
                // Create the search user nearby service
                val searchUserService : GetUserWithinARadiusService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    GetUserWithinARadiusService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = searchUserService.getUserWithinARadius("${lastUpdatedLocation.latitude},${lastUpdatedLocation.longitude}", 50, "km", searchQuery)

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

                            // Get data from the response body (array of found users)
                            val listOfFoundUsers = responseBody["data"] as ArrayList<User>

                            // Return list of users to view model via callback function
                            callback(listOfFoundUsers)
                        } else {
                            print("Something is not right")
                        }
                    }
                })
            }
        }
    }

    // The function to get list of followers of user with specified user id
    fun getListOfLikes (userId: String, callback: (arrayOfLikes: ArrayList<String>) -> Unit) {
        // Create the service for getting array of followers (we will get number of followers based on that)
        val getArrayOfFollowersService: GeteFollowerService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
            GeteFollowerService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getArrayOfFollowersService.getFollowers(userId)

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

                    // Get list of followers
                    val listOfFollowers = data["documents"] as ArrayList<Map<String, Any>>

                    // Return list of followers to view model via callback function

                }
            }
        })
    }
}