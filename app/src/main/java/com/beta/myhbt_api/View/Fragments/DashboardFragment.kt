package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Controller.Posts.GetFirstImageURLOfPostService
import com.beta.myhbt_api.Interfaces.PostShowingInterface
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.R
import com.beta.myhbt_api.Repository.NotificationRepositories.NotificationRepository
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPost
import com.beta.myhbt_api.ViewModel.PostViewModel
import kotlinx.android.synthetic.main.fragment_dashboard.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DashboardFragment : Fragment(), PostShowingInterface {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Array of HBTGram posts
    private var hbtGramPosts = ArrayList<HBTGramPost>()

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterHBTGramPost?= null

    // Location in list for next load (the variable which will keep track of from where to load next posts for the user)
    private var locationInListForNextLoad: Int = 0

    // User id of the currently logged in user
    private var userIdOfCurrentUser: String = ""

    // The post view model
    private lateinit var postViewModel: PostViewModel

    // The user repository
    private lateinit var userInfoRepository: UserRepository

    // The notification repository
    private lateinit var notificationRepository: NotificationRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the post view model
        postViewModel = PostViewModel(this.requireContext())

        // Instantiate the user info repository
        userInfoRepository =
            UserRepository(
                executorService,
                this.requireContext()
            )

        // Instantiate the notification repository
        notificationRepository =
            NotificationRepository(
                executorService,
                this.requireContext()
            )

        // Show the loading layout and hide the recycler view at beginning
        loadingLayoutHomePage.visibility = View.VISIBLE
        hbtGramView.visibility = View.INVISIBLE

        // Instantiate the recycler view
        hbtGramView.layoutManager = LinearLayoutManager(this@DashboardFragment.context)
        hbtGramView.itemAnimator = DefaultItemAnimator()

        // Call the function to get info of the currently logged in user
        userInfoRepository.getInfoOfCurrentUser { userObject ->
            // Update current user info in this activity
            userIdOfCurrentUser = userObject.getId()

            // Update the adapter
            adapter = RecyclerViewAdapterHBTGramPost(hbtGramPosts, this@DashboardFragment.requireActivity(), this@DashboardFragment, executorService, userObject)

            // Add adapter to the RecyclerView
            hbtGramView.adapter = adapter

            // Call the function to start loading posts
            getAllPost(userIdOfCurrentUser)
        }

        // Call the function to get posts for the user
        //getInfoOfCurrentUserAndLoadPosts()
    }

    //*********************************** GET POSTS SEQUENCE ***********************************
    /*
    In this sequence, we will do these things
    1. Get info of the currently logged in user
    2. Get current location of the user (already done earlier)
    3. Get info of the latest post in the database (to start loading)
     */

    // The function to get all posts from the database for user
    private fun getAllPost (userId: String) {
        // Call the function in the view model to start loading posts
        postViewModel.loadPosts(userId) {postArray, newCurrentLocationInList ->
            // Update new current location in list (location in list for next load)
            // If order in collection to load next series of post is null, let it be 0
            locationInListForNextLoad = newCurrentLocationInList

            // Update the array list of posts
            hbtGramPosts.addAll(postArray)

            // Update the RecyclerView
            hbtGramView.adapter!!.notifyDataSetChanged()

            // Show the recycler view and hide the loading layout when done at beginning
            loadingLayoutHomePage.visibility = View.INVISIBLE
            hbtGramView.visibility = View.VISIBLE
        }
    }
    //*********************************** END GET POSTS SEQUENCE ***********************************

    //*********************************** IMPLEMENT ABSTRACT FUNCTION OF THE INTERFACE TO LOAD MORE POSTS ***********************************
    override fun loadMorePosts() {
        // Call the function in the view model to load more posts
        postViewModel.loadMorePosts(userIdOfCurrentUser, locationInListForNextLoad) {postArray, newCurrentLocationInList ->
            // Update new current location in list (location in list for next load)
            // If order in collection to load next series of post is null, let it be 0
            locationInListForNextLoad = newCurrentLocationInList

            // Update the array list of posts
            hbtGramPosts.addAll(postArray)

            // Update the RecyclerView
            hbtGramView.adapter!!.notifyDataSetChanged()
        }
    }
    //*********************************** END IMPLEMENT ABSTRACT FUNCTION OF THE INTERFACE TO LOAD MORE POSTS ***********************************

    //******************************** CREATE NOTIFICATION SEQUENCE (IMPLEMENTED FROM INTERFACE) ********************************
    // The function to create new notification. It should load first photo of post first
    override fun createNotification (content: String, forUser: String, fromUser: String, image: String, postId: String) {
        // Create the get first image URL service
        val getFirstImageURLService: GetFirstImageURLOfPostService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
            GetFirstImageURLOfPostService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getFirstImageURLService.getFirstPhotoURL(postId)

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

                    // Get the array of images
                    val arrayOfImages = data["documents"] as List<Map<String, Any>>

                    if (arrayOfImages.isNotEmpty()) {
                        // Get image info from the received data
                        val firstImageInfo = (data["documents"] as List<Map<String, Any>>)[0]

                        // Get URL of the image
                        val firstImageURL = firstImageInfo["imageURL"] as String

                        // Call the function to actually create the notification
                        notificationRepository.sendNotification(content, forUser, fromUser, firstImageURL, postId)
                    }
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //******************************** END CREATE NOTIFICATION SEQUENCE (IMPLEMENTED FROM INTERFACE) ********************************
}