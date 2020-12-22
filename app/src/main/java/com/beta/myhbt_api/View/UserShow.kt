package com.beta.myhbt_api.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterUserShow
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_user_show.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserShow : AppCompatActivity() {
    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterUserShow

    // The variable to keep track of which list of users to show at this activity
    private var whatToDo = ""

    // Post id of the post to show list of likes of (in case the activity suppose to show list of likes)
    private var postId = ""

    // User id of the user to show list of followers or following of (in case the activity suppose to show list of followers or followings)
    private var userId = ""

    // List of users to be shown
    private var arrayOfUsers = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_show)

        // Get what to do next from the previous activity
        whatToDo = intent.getStringExtra("whatToDo")!!

        // Get post id of the selected post from the previous activity
        postId = intent.getStringExtra("postId")!!

        // Get user id of the user to show list of followers or followings from the previous activity
        userId = intent.getStringExtra("userId")

        // Instantiate the recycler view
        userShowView.layoutManager = LinearLayoutManager(applicationContext)
        userShowView.itemAnimator = DefaultItemAnimator()

        // Update the adapter
        adapter = RecyclerViewAdapterUserShow(
            arrayOfUsers,
            this
        )

        // Add adapter to the recycler view
        userShowView.adapter = adapter

        // Based on the variable which specify what to do next, call the right function
        when (whatToDo) {
            "getListOfLikes" -> {
                // Call the function to get list of likes of the post with specified id
                getListOfLikes(postId)
            } // if What to do next is to get list of followers, call the function to get list
            // of followers
            "getListOfFollowers" -> {
                getListOfFollowers(userId)
            } // If what to do next is to get list of followings, call the function to get list
            // of followings
            else -> {
                getListOfFollowings(userId)
            }
        }
    }

    //************************************** GET LIST OF LIKES SEQUENCE **************************************
    /*
    In this sequence, we will do 2 things
    1. Get list of likes of the post (this will include list of user ids who like the post)
    2. Get user info of those users based on their id
     */

    // The function to get list of likes of the post
    private fun getListOfLikes (postId: String) {
        // Create the get post likes service
        val getPostLikesService: GetAllHBTGramPostLikesService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetAllHBTGramPostLikesService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getPostLikesService.getPostLikes(postId)

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

                    // Get data of the response
                    val data = responseBody["data"] as Map<String, Any>

                    // Get data from the response
                    val listOfLikes = data["documents"] as ArrayList<Map<String, Any>>

                    // Loop through that list of likes, get liker info based on their id
                    for (like in listOfLikes) {
                        // Call the function to get user info based on their id
                        getUserInfoBasedOnId(like["whoLike"] as String)
                    }
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //************************************** END GET LIST OF LIKES SEQUENCE **************************************

    //************************************** GET LIST OF FOLLOWERS SEQUENCE **************************************
    /*
    In this sequence, we will do 2 things
    1. Get list of followers of the user (this will include list of their user ids)
    2. Get user info of those users based on their id
     */

    // The function to get list of followers of the user
    private fun getListOfFollowers (userId: String) {
        // Create the service for getting array of followers (we will get number of followers based on that)
        val getArrayOfFollowersService: GeteFollowerService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
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

                    // Loop through that list of followers, get follower info based on their id
                    for (follower in listOfFollowers) {
                        // Call the function to get user info based on their id
                        getUserInfoBasedOnId(follower["follower"] as String)
                    }
                }
            }
        })
    }
    //************************************** END GET LIST OF FOLLOWERS SEQUENCE **************************************

    //************************************** GET LIST OF FOLLOWINGS SEQUENCE **************************************
    /*
    In this sequence, we will do 2 things
    1. Get list of followings of the user (this will include list of their user ids)
    2. Get user info of those users based on their id
     */

    // The function to get list of followings of the user
    private fun getListOfFollowings (userId: String) {
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

                    // Get list of followings
                    val listOfFollowings = data["documents"] as ArrayList<Map<String, Any>>

                    // Loop through that list of followings, get follower info based on their id
                    for (following in listOfFollowings) {
                        // Call the function to get user info based on their id
                        getUserInfoBasedOnId(following["following"] as String)
                    }
                }
            }
        })
    }
    //************************************** END GET LIST OF FOLLOWINGS SEQUENCE **************************************

    //************************************** GET LIST OF USERS BASED ON LIST OF IDS **************************************
    // The function to get user info based on id
    private fun getUserInfoBasedOnId (userId: String) {
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

                    // Get user info from the data (it will be linked tree map at this point)
                    val userData = (data["documents"] as List<Map<String, Any>>)[0]

                    // In order to prevent us from encountering the class cast exception, we need to do the following
                    // Create the GSON object
                    val gs = Gson()

                    // Convert a linked tree map into a JSON string
                    val jsUser = gs.toJson(userData)

                    // Convert the JSOn string back into HBTGramPost class
                    val userInfo = gs.fromJson<User>(jsUser, User::class.java)

                    // Add user object to the array of users
                    arrayOfUsers.add(userInfo)

                    // Reload the recycler view
                    userShowView.adapter!!.notifyDataSetChanged()
                }
            }
        })
    }
    //************************************** END GET LIST OF USERS BASED ON LIST OF IDS **************************************
}