package com.beta.cuckoo.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Network.*
import com.beta.cuckoo.Network.Notifications.CreateNotificationService
import com.beta.cuckoo.Network.Posts.GetFirstImageURLOfPostService
import com.beta.cuckoo.Interfaces.PostShowingInterface
import com.beta.cuckoo.Model.CuckooPost
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.PostRepositories.PostRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterCuckooPost
import kotlinx.android.synthetic.main.fragment_dashboard.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PostsAroundFragment : Fragment(), PostShowingInterface {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // The user repository
    private lateinit var userInfoRepository: UserRepository

    // Array of HBTGram posts nearby
    private var hbtGramPosts = ArrayList<CuckooPost>()

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterCuckooPost?= null

    // Location in list for next load (the variable which will keep track of from where to load next posts for the user)
    private var locationInListForNextLoad: Int = 0

    // User id of the currently logged in user
    private var userIdOfCurrentUser: String = ""

    // The post repository
    private lateinit var postRepository: PostRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the post repository
        postRepository = PostRepository(executorService, this.requireContext())

        // Show the loading layout and hide the recycler view at beginning
        loadingLayoutHomePage.visibility = View.VISIBLE
        hbtGramView.visibility = View.INVISIBLE

        // Instantiate the user info repository
        userInfoRepository =
            UserRepository(
                executorService,
                this.requireContext()
            )

        // Instantiate the recycler view
        hbtGramView.layoutManager = LinearLayoutManager(this@PostsAroundFragment.context)
        hbtGramView.itemAnimator = DefaultItemAnimator()

        // Call the function to get info of the currently logged in user
        userInfoRepository.getInfoOfCurrentUser { userObject ->
            // Update current user info in this activity
            userIdOfCurrentUser = userObject.getId()

            // Update the adapter
            adapter = RecyclerViewAdapterCuckooPost(hbtGramPosts, this@PostsAroundFragment.requireActivity(), this@PostsAroundFragment, executorService, userObject)

            // Add adapter to the RecyclerView
            hbtGramView.adapter = adapter

            // Call the function to get posts around last updated location of the currently logged in user
            getPostsAroundCurrentUser()
        }
    }

    //*************************** GET POSTS AROUND SEQUENCE ***************************
    /*
    In this sequence, we will do 2 things
    1. Get order in collection of latest post in collection
    2. Start loading posts from that location
     */

    // The function to load posts within a radius around last updated location of the currently logged in user
    private fun getPostsAroundCurrentUser () {
        // Call the function to get posts around last updated location of the currently logged in user
        postRepository.getPostAroundOfCurrentUser {arrayOfPosts, locationForNextLoad ->
            // Update new current location in list (location in list for next load)
            // If order in collection to load next series of post is null, let it be 0
            locationInListForNextLoad = locationForNextLoad

            // Update the array list of posts
            hbtGramPosts.addAll(arrayOfPosts)

            // Update the RecyclerView
            hbtGramView.adapter!!.notifyDataSetChanged()

            // Show the recycler view and hide the loading layout when done at beginning
            loadingLayoutHomePage.visibility = View.INVISIBLE
            hbtGramView.visibility = View.VISIBLE
        }
    }
    //*************************** END GET POSTS AROUND SEQUENCE ***************************

    //*********************************** IMPLEMENT ABSTRACT FUNCTION OF THE INTERFACE TO LOAD MORE POSTS ***********************************
    override fun loadMorePosts() {
        // Call the function to load more posts
        //getPostsAround()
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
                        sendNotification(content, forUser, fromUser, firstImageURL, postId)
                    }
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to send notification
    private fun sendNotification (content: String, forUser: String, fromUser: String, image: String, postId: String) {
        // Create the create notification service
        val createNotificationService: CreateNotificationService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
            CreateNotificationService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = createNotificationService.createNewNotification(content, forUser, fromUser, image, postId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {

            }
        })
    }
    //******************************** END CREATE NOTIFICATION SEQUENCE (IMPLEMENTED FROM INTERFACE) ********************************
}