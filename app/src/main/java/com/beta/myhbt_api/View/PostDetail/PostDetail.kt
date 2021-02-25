package com.beta.myhbt_api.View.PostDetail

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Network.*
import com.beta.myhbt_api.Network.Notifications.CreateNotificationService
import com.beta.myhbt_api.Interfaces.CreateNotificationInterface
import com.beta.myhbt_api.Model.CuckooPost
import com.beta.myhbt_api.Model.PostComment
import com.beta.myhbt_api.Model.PostPhoto
import com.beta.myhbt_api.R
import com.beta.myhbt_api.Repository.PostRepositories.PhotoRepository
import com.beta.myhbt_api.Repository.PostRepositories.PostRepository
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterCuckooPostDetail
import com.beta.myhbt_api.View.MainMenu.MainMenu
import com.beta.myhbt_api.View.Menus.CommentOptionsMenu
import com.beta.myhbt_api.View.Menus.PostOptionsMenu
import com.beta.myhbt_api.ViewModel.PostViewModel
import com.google.gson.Gson
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_hbtgram_post_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PostDetail : AppCompatActivity(), CreateNotificationInterface {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // The user view model
    private lateinit var userRepository: UserRepository

    // The post view model
    private lateinit var postViewModel: PostViewModel

    // The post repository
    private lateinit var postRepository: PostRepository

    // The photo repository
    private lateinit var photoRepository: PhotoRepository

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterCuckooPostDetail?= null

    // Selected post object
    private var selectedPostObject = CuckooPost("", "", "", 0, 0, "")

    // Array of image URL of the post
    private var arrayOfImages = ArrayList<PostPhoto>()

    // Array of comments of the post
    private var arrayOfComments = ArrayList<PostComment>()

    // In order to prevent us from encountering the class cast exception, we need to do the following
    // Create the GSON object
    private val gs = Gson()

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hbtgram_post_detail)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate the user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate the post repository
        postRepository = PostRepository(executorService, applicationContext)

        // Instantiate the photo repository
        photoRepository = PhotoRepository(executorService, applicationContext)

        // Instantiate the post view model
        postViewModel = PostViewModel(applicationContext)

        // Set on click listener for the back button
        backButtonPostDetail.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Set on click listener for the open post options menu button
        postOptionsMenu.setOnClickListener {
            // The bottom sheet object (post options menu)
            val bottomSheet = PostOptionsMenu(this, selectedPostObject.getId(), executorService)

            // Show the menu
            bottomSheet.show(supportFragmentManager, "TAG")
        }

        // Get the selected post object from previous activity
        selectedPostObject = intent.getSerializableExtra("selectedPostObject") as CuckooPost

        // Instantiate the recycler view
        hbtGramPostDetailView.layoutManager = LinearLayoutManager(applicationContext)
        hbtGramPostDetailView.itemAnimator = DefaultItemAnimator()

        // Set up on click listener for the post comment button
        postCommentButtonPostDetail.setOnClickListener {
            // Call the function to create new comment sent to the post by the current user
            createNewComment(commentContentToPostPostDetail, selectedPostObject.getId(), selectedPostObject.getWriter())
        }

        // Set up on click listener for the send image as comment button
        selectPictureForCommentButton.setOnClickListener {
            // Start the activity where the user can choose image to send
            // The intent object
            val intent = Intent(applicationContext, PostDetailCommentSendImage::class.java)

            // Put post id into the intent so that next activity will know which post to work with
            intent.putExtra("postId", selectedPostObject.getId())

            // Start the activity
            startActivity(intent)
        }

        // Call the function to get post detail
        getPostDetail(selectedPostObject.getId())

        // Call the function to set up socket.io and make everything real time
        setUpSocketIO()
    }


    // THe function to set up socket.IO
    private fun setUpSocketIO () {
        //************************ DO THINGS WITH THE SOCKET.IO ************************
        // Bring user into the post detail room
        MainMenu.mSocket.emit("jumpInPostDetailRoom", gs.toJson(hashMapOf(
            "postId" to selectedPostObject.getId()
        )))

        // Listen to event of when new comment is added to the post
        MainMenu.mSocket.on("updateComment", onUpdateComment)

        // Listen to event of when new comment with photo is added to the post
        MainMenu.mSocket.on("updateCommentWithPhoto", onUpdateCommentWithPhoto)
        //************************ END WORKING WITH SOCKET.IO ************************v
    }

    //************************* CALL BACK FUNCTIONS FOR SOCKET.IO *************************
    // The callback function to update comment when the new one is added to the post
    private var onUpdateComment = Emitter.Listener {
        // New comment object from the server
        val commentObject: PostComment = gs.fromJson(it[0].toString(), PostComment::class.java)

        // Since this will update the view, it MUST run on the UI thread
        runOnUiThread{
            // Add new comment to the array of comments
            arrayOfComments.add(commentObject)

            // Update the RecyclerView
            hbtGramPostDetailView.adapter!!.notifyDataSetChanged()
        }
    }

    // The callback function to update comment when new one with photo is added to the post
    private var onUpdateCommentWithPhoto = Emitter.Listener {
        // New comment object from the server
        val commentObject: PostComment = gs.fromJson(it[0].toString(), PostComment::class.java)

        // Since this will update the view, it MUST run on the UI thread
        runOnUiThread{
            // Add new comment to the array of comments
            arrayOfComments.add(commentObject)

            // Update the RecyclerView
            hbtGramPostDetailView.adapter!!.notifyDataSetChanged()
        }
    }
    //************************* END CALL BACK FUNCTIONS FOR SOCKET.IO *************************

    //*********************************** CREATE NEW COMMENT SEQUENCE ***********************************
    // The function to create new comment for the post
    private fun createNewComment (commentContentToPostEditText: EditText, postId: String, commentReceiverUserId: String) {
        // Call the function to create new comment for the post
        postRepository.createCommentForPost(commentContentToPostEditText.text.toString(), postId) {commentWriterId ->
            //-------------- Image for the notification --------------
            // Convert photo object which is currently a linked tree map into a JSON string
            val jsPhoto = gs.toJson(arrayOfImages[0])

            // Convert the JSOn string back into PostPhoto class
            val photoObject = gs.fromJson<PostPhoto>(jsPhoto, PostPhoto::class.java)

            // Call the function to create new notification for the post writer
            createNotification("commented", commentReceiverUserId, commentWriterId, photoObject.getImageURL(), selectedPostObject.getId())
            //-------------- Image for the notification --------------

            //-------------- Update the UI --------------
            // Create new comment object based on info of the newly created comment
            val newCommentObject = PostComment(commentContentToPostEditText.text.toString(), commentWriterId, "")

            // Add the newly created comment object to the array
            arrayOfComments.add(newCommentObject)

            // Empty content of the EditText
            commentContentToPostEditText.setText("")

            // Reload the RecyclerView
            hbtGramPostDetailView.adapter!!.notifyDataSetChanged()
            //-------------- End update the UI --------------

            // Emit event to the server and let the server know that new comment has been added
            MainMenu.mSocket.emit("newComment", gs.toJson(hashMapOf(
                "commentId" to "",
                "writer" to commentWriterId,
                "content" to commentContentToPostEditText.text.toString(),
                "postId" to selectedPostObject.getId()
            )))
        }
    }
    //*********************************** END CREATE NEW COMMENT SEQUENCE ***********************************

    //*********************************** GET POST DETAIL SEQUENCE ***********************************
    // The function to load post info of the selected post
    private fun getPostDetail (postId: String) {
        // Call the function to get detail info of the post
        postViewModel.getPostDetail(postId) {arrayOfImages, arrayOfComments ->
            // Update adapter for the RecyclerView
            adapter = RecyclerViewAdapterCuckooPostDetail(selectedPostObject, arrayOfImages, arrayOfComments, this@PostDetail, this, this, executorService)

            // Add adapter to the RecyclerView
            hbtGramPostDetailView.adapter = adapter

            // Loop through list of photos of the post to update user photo label visit status
            for (i in 0 until arrayOfImages.size) {
                // Convert the image object which is currently a linked tree map into a JSON string
                val jsPhoto = gs.toJson(arrayOfImages[i])

                // Convert the JSON string back into User class
                val photoObject = gs.fromJson<PostPhoto>(jsPhoto, PostPhoto::class.java)

                // Call the function to update user photo label visit status for the current user
                updatePhotoLabelVisitStatusOfCurrentUser(photoObject.getPhotoId())
            }
        }
    }
    //*********************************** END GET POST DETAIL SEQUENCE ***********************************

    //*********************************** GET AND UPDATE PHOTO LABEL VISIT ***********************************
    // The function to update photo label visit of the currently logged in user
    private fun updatePhotoLabelVisitStatusOfCurrentUser (photoId: String) {
        // Call the function to get photo labels of the photo with specified photo id
        photoRepository.getPhotoLabelsBasedOnId(photoId) {arrayOfPhotoLabels ->
            // Loop through the list of photo labels to update photo label visit status of the current user
            for (photoLabel in arrayOfPhotoLabels) {
                // Call the function to update photo label visit status of the currently logged in user
                photoRepository.updatePhotoLabelVisitForCurrentUser(photoLabel) {}
            }
        }
    }
    //*********************************** END GET AND UPDATE PHOTO LABEL VISIT ***********************************

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

            }
        })
    }
    //******************************** END CREATE NOTIFICATION SEQUENCE ********************************

    //******************************** OPEN COMMENT OPTIONS MENU SEQUENCE ********************************
    fun openCommentOptionsMenu(commentId: String) {
        // The bottom sheet object (comment options menu)
        val bottomSheet = CommentOptionsMenu(this, commentId)

        // Show the menu
        bottomSheet.show(supportFragmentManager, "TAG")
    }
    //******************************** END OPEN COMMENT OPTIONS MENU SEQUENCE ********************************
}
