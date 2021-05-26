package com.beta.cuckoo.Repository.UserRepositories

import android.content.Context
import android.widget.Toast
import com.beta.cuckoo.Network.*
import com.beta.cuckoo.Network.Follows.GeteFollowerService
import com.beta.cuckoo.Network.User.*
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.Network.Follows.GetFollowingService
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mapbox.mapboxsdk.geometry.LatLng
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap
import java.util.concurrent.Executor

class UserRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // Instance of FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

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

    // The function to get user of user based on specified user id
    fun getBioOfUserWithId (userId: String, callback: (userBio: String) -> Unit) {
        // Create the get user info service
        val getUserInfoService: GetUserInfoBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
            GetUserInfoBasedOnIdService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getUserInfoService.getUserInfoBasedOnId(userId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is data
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    // Get obtain user data from that
                    val data = (((responseBody["data"] as Map<String, Any>)["documents"]) as List<Map<String, Any>>)[0]

                    // Load bio of the user
                    val userBio = data["description"] as String

                    // Return user bio via callback function
                    callback(userBio)
                } else {
                    print("Something is not right")
                }
            }
        })
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
    fun signUp (fullName: String, email: String, password: String, confirmPassword: String, callback: (signUpSuccess: Boolean, errorMessage: String) -> Unit) {
        executor.execute{
            // Create the sign up service
            val signUpService : SignUpService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                SignUpService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = signUpService.signUp(email, password, confirmPassword, fullName, "avatar", "cover")

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If status of the call is 200, it means that sign up is done,
                    // let the view know that and pass nothing as error message
                    if (response.code() == 200) {
                        // Let the view model know that sign up is success via callback function
                        callback(true, "")
                    } // Otherwise, show error
                    else {
                        // Get error body
                        val errorBody = response.errorBody()

                        // Parse error body into hash map
                        val errorHashMap = Gson().fromJson(errorBody!!.string(), HashMap::class.java)

                        // Let the view model know that sign up is not done via callback function
                        callback(false, errorHashMap["data"] as String)
                    }
                }
            })
        }
    }

    // The function to sign out
    fun signOut (callback: () -> Unit) {
        // Sign the current user out
        mAuth.signOut()

        // Let the view know that sign out is done via callback function
        callback()
        /*
        // Create the post service
        val postService: LogoutPostDataService = RetrofitClientInstance.getRetrofitInstance(context)!!
            .create(LogoutPostDataService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = postService.logout()

        // Perform the API call
        call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is null, it means that the user may didn't enter the correct email or password
                if (response.body() == null) {
                    // Show the user that the login was not successful
                    Toast.makeText(context, "Something is not right", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // Let the view know that sign out is done via callback function
                    callback()
                }
            }
        })
         */
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

    // The function to get list of followers of the user with specified user id
    fun getListOfFollowers (userId: String, callback: (listOfUserId: ArrayList<String>) -> Unit) {
        executor.execute {
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
                        // Array of user id of followers
                        val arrayOfFollowerUserId = ArrayList<String>()

                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data of the response
                        val data = responseBody["data"] as Map<String, Any>

                        // Get list of followers
                        val listOfFollowers = data["documents"] as ArrayList<Map<String, Any>>

                        // Loop through that list of followers, get follower info based on their id
                        for (follower in listOfFollowers) {
                            // Add user id of follower to the array of follower user id
                            arrayOfFollowerUserId.add(follower["follower"] as String)
                        }

                        // Return list of follower user id via callback function
                        callback(arrayOfFollowerUserId)
                    }
                }
            })
        }
    }

    // The function to get list of following of user with specified user id
    fun getListOfFollowing (userId: String, callback: (listOfUserId: ArrayList<String>) -> Unit) {
        executor.execute {
            // Create the service for getting number of followings
            val getArrayOfFollowingService: GetFollowingService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
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
                        // Array of user id of followings
                        val arrayOfFollowingsUserId = ArrayList<String>()

                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data of the response
                        val data = responseBody["data"] as Map<String, Any>

                        // Get list of followings
                        val listOfFollowings = data["documents"] as ArrayList<Map<String, Any>>

                        // Loop through that list of followings, get follower info based on their id
                        for (following in listOfFollowings) {
                            // Add user id of following to the array of following user id
                            arrayOfFollowingsUserId.add(following["following"] as String)
                        }

                        // Return array of following user id via callback function
                        callback(arrayOfFollowingsUserId)
                    }
                }
            })
        }
    }

    // The function to update info of the currently logged in user
    fun updateCurrentUserInfo (mapOfFields: Map<String, Any>, callback: (done: Boolean) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            getInfoOfCurrentUser { userObject ->
                // Create the update user info service
                val updateUserInfoService: UpdateUserInfoService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    UpdateUserInfoService::class.java)

                // Create the call object in order to perform the call
                val call: Call<Any> = updateUserInfoService.updateUserInfo(
                    mapOfFields["email"] as String,
                    mapOfFields["avatarURL"] as String,
                    mapOfFields["coverURL"] as String,
                    userObject.getId()
                )

                // Perform the API call
                call.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        // Report the error if something is not right
                        print("Boom")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // If the response body is null, it means that the user may didn't enter the correct email or password
                        if (response.body() == null) {
                            // Show the user that user update was not successful
                            Toast.makeText(context, "Something is not right", Toast.LENGTH_SHORT).show()

                            // Let the view know that user info was not updated via callback function
                            callback(false)
                        } else {
                            // Show the user that user update was successful
                            Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()

                            // Let the view know that user info was updated via callback function
                            callback(true)
                        }
                    }
                })
            }
        }
    }
}