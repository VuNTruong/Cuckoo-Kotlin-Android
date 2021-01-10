package com.beta.myhbt_api.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.BackgroundServices
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Interfaces.CreateNotificationInterface
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterProfileDetail
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_profile_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Array

class ProfileDetail : AppCompatActivity(), CreateNotificationInterface {
    // These objects are used for socket.io
    private val gson = Gson()

    // Adapter for the RecyclerView
    private lateinit var adapter: RecyclerViewAdapterProfileDetail

    // User id of the currently logged in user
    private lateinit var currentUserId: String

    // User object of the user
    private var userObject = User("", "", "", "", "", "", "", "","", "", "", "", "")

    // Array of images
    private var arrayOfImages = ArrayList<HBTGramPostPhoto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)

        // Hide the action bar
        supportActionBar!!.hide()

        // Get selected user object from the previous activity
        userObject = intent.getSerializableExtra("selectedUserObject") as User

        // Instantiate the recycler view
        profileDetailView.layoutManager = LinearLayoutManager(applicationContext)
        profileDetailView.itemAnimator = DefaultItemAnimator()

        // Call the function to load further info of the user
        getInfoOfCurrentUserAndFurtherInfo()
    }

    //******************************************* LOAD INFO OF USER SEQUENCE *******************************************
    // The function to get id of current user which will then check if user at this activity is current or not
    private fun getInfoOfCurrentUserAndFurtherInfo () {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
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
                    val body = response.body()
                    print(body)
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user id in the database of the currently logged in user
                    val userId = data["_id"] as String

                    // Update current user id property of this activity
                    currentUserId = userId

                    // Call the function to update user profile visit
                    updateUserProfileVisit()

                    // Check to see if user object at this activity is the current user or not, then call
                    // the function to set up rest of the view
                    if (userId == userObject.getId()) {
                        // Call the function to set up the rest as well as let the function know that user at this activity is the current user
                        loadPhotosOfUser(userObject.getId(), true)
                    } // Otherwise, call the function to set up the rest and let it know that user at this activity is not the current user
                    else {
                        loadPhotosOfUser(userObject.getId(), false)
                    }
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to load all photos created by the user
    fun loadPhotosOfUser (userId: String, currentUser: Boolean) {
        // Create the get images of user service
        val getPhotosOfUserService: GetPhotosOfUserService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetPhotosOfUserService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getPhotosOfUserService.getPhotosOfUser(userId)

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

                    // Get data from the response body (array of images created by the user)
                    val data = responseBody["data"] as ArrayList<HBTGramPostPhoto>

                    // Set the array of images be the one we just got
                    arrayOfImages = data

                    // Update the adapter
                    adapter = RecyclerViewAdapterProfileDetail(arrayOfImages, userObject, this@ProfileDetail, currentUser)

                    // Add adapter to the RecyclerView
                    profileDetailView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //******************************************* END LOAD INFO OF USER SEQUENCE *******************************************

    //******************************************* UPDATE PROFILE VISIT SEQUENCE *******************************************
    /*
    In this sequence, we will check and see if user shown at this activity is the currently logged in user or not
    If not, update number of time profile is visited by the currently logged in user
    If it is, don't update anything
     */

    // The function to update user profile visit
    private fun updateUserProfileVisit () {
        // Check to see if user shown at this activity is the currently logged in user or not
        if (currentUserId == userObject.getId()) {
            // If it is, don't update anything
            return
        }

        // Create the update user profile visit service
        val updateUserProfileVisitService: UpdateUserProfileVisitService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            UpdateUserProfileVisitService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = updateUserProfileVisitService.updateProfileVisit(currentUserId, userObject.getId())

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    print("Done")
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //******************************************* END UPDATE PROFILE VISIT SEQUENCE *******************************************

    //******************************** CREATE NOTIFICATION SEQUENCE ********************************
    // The function to create new notification
    override fun createNotification (content: String, forUser: String, fromUser: String, image: String, postId: String) {
        // Create the create notification service
        val createNotificationService: CreateNotificationService = RetrofitClientInstance.getRetrofitInstance(this)!!.create(
            CreateNotificationService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = createNotificationService.createNewNotification(content, forUser, fromUser, image, postId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // Emit event to the server so that the server know to send notification to the user that get followed
                BackgroundServices.mSocket.emit("newFollow", gson.toJson(hashMapOf(
                    "follower" to fromUser,
                    "followedUser" to forUser
                )))
            }
        })
    }
    //******************************** END CREATE NOTIFICATION SEQUENCE ********************************
}
