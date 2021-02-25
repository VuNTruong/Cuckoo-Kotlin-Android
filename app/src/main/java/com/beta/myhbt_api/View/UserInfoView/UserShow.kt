package com.beta.myhbt_api.View.UserInfoView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Network.*
import com.beta.myhbt_api.Network.Follows.GetFollowingService
import com.beta.myhbt_api.Network.Follows.GeteFollowerService
import com.beta.myhbt_api.Network.LikesAndComments.GetAllPostLikesService
import com.beta.myhbt_api.Network.User.GetUserInfoBasedOnIdService
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterUserShow
import com.beta.myhbt_api.ViewModel.PostViewModel
import com.beta.myhbt_api.ViewModel.UserViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_user_show.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UserShow : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Post view model
    private lateinit var postViewModel: PostViewModel

    // User view model
    private lateinit var userViewModel: UserViewModel

    // User repository
    private lateinit var userRepository: UserRepository

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

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_show)

        // Instantiate the post view model
        postViewModel = PostViewModel(applicationContext)

        // Instantiate the user view model
        userViewModel = UserViewModel(applicationContext)

        // Instantiate the user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Hide the action bar
        supportActionBar!!.hide()

        // Set on click listener for the back button
        backButtonUserShow.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Get what to do next from the previous activity
        whatToDo = intent.getStringExtra("whatToDo")!!

        // Get post id of the selected post from the previous activity
        postId = intent.getStringExtra("postId")!!

        // Get user id of the user to show list of followers or followings from the previous activity
        userId = intent.getStringExtra("userId")!!

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
        // Call the function to get list of user objects of users who like post with the specified user id
        postViewModel.getListOfUserWhoLikePost(postId) {arrayOfUsersWhoLike ->
            // Loop through list of user ids of user who like post of the specified id to get their user objects
            for (userId in arrayOfUsersWhoLike) {
                // Call the function to get user object based on user id
                userRepository.getUserInfoBasedOnId(userId) {userObject ->
                    // Add user object to the array of users
                    arrayOfUsers.add(userObject)

                    // Reload the recycler view
                    userShowView.adapter!!.notifyDataSetChanged()
                }
            }
        }
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
        // Call the function to get list of user objects of users who follow user with specified user id
        userViewModel.getListOfFollowerUserObject(userId) {arrayOfUserId ->
            // Loop through list of user ids of followers to get their user objects
            for (userId in arrayOfUserId) {
                // Call the function to get user object based on user id
                userRepository.getUserInfoBasedOnId(userId) {userObject ->
                    // Add user object to the array of users
                    arrayOfUsers.add(userObject)

                    // Reload the recycler view
                    userShowView.adapter!!.notifyDataSetChanged()
                }
            }
        }
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
        // Call the function to get list of user objects of users to whom user with specified user id is following
        userViewModel.getListOfFollowingUserObject(userId) {arrayOfUserId ->
            // Loop through list of user ids of followings to get their user objects
            for (userId in arrayOfUserId) {
                // Call the function to get user object based on user id
                userRepository.getUserInfoBasedOnId(userId) {userObject ->
                    // Add user object to the array of users
                    arrayOfUsers.add(userObject)

                    // Reload the recycler view
                    userShowView.adapter!!.notifyDataSetChanged()
                }
            }
        }
    }
    //************************************** END GET LIST OF FOLLOWINGS SEQUENCE **************************************
}