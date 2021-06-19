package com.beta.cuckoo.View.PostDetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.CuckooPost
import com.beta.cuckoo.Model.PostComment
import com.beta.cuckoo.Model.PostPhoto
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.NotificationRepositories.NotificationRepository
import com.beta.cuckoo.Repository.PostRepositories.PhotoRepository
import com.beta.cuckoo.Repository.PostRepositories.PostRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterCuckooPostDetail
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.beta.cuckoo.View.Menus.CommentOptionsMenu
import com.beta.cuckoo.View.Menus.PostDetailSendImageMenu
import com.beta.cuckoo.View.Menus.PostOptionsMenu
import com.beta.cuckoo.ViewModel.PostViewModel
import com.google.gson.Gson
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_post_detail.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PostDetail : AppCompatActivity() {
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

    // Notification repository
    private lateinit var notificationRepository: NotificationRepository

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
        setContentView(R.layout.activity_post_detail)

        // Hide the action bar
        supportActionBar!!.hide()

        // Set this up so that keyboard won't push the whole layout up
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        // Instantiate the user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate the post repository
        postRepository = PostRepository(executorService, applicationContext)

        // Instantiate the photo repository
        photoRepository = PhotoRepository(executorService, applicationContext)

        // Instantiate notification repository
        notificationRepository = NotificationRepository(executorService, applicationContext)

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

        // Call the function to check and see if post being shown here is created by currently logged in user or not
        userRepository.getInfoOfCurrentUser { userObject ->
            // If it is, show the post options menu button
            if (userObject.getId() == selectedPostObject.getWriter()) {
                // Show the post options menu button
                postOptionsMenu.visibility = View.VISIBLE
            } // Otherwise, hide it
            else {
                // Hide the post options menu button
                postOptionsMenu.visibility = View.INVISIBLE
            }
        }

        // Get the selected post object from previous activity
        selectedPostObject = intent.getSerializableExtra("selectedPostObject") as CuckooPost

        // Instantiate the recycler view
        postDetailView.layoutManager = LinearLayoutManager(applicationContext)
        postDetailView.itemAnimator = DefaultItemAnimator()

        // Set up on click listener for the post comment button
        postCommentButtonPostDetail.setOnClickListener {
            // Call the function to create new comment sent to the post by the current user
            createNewComment(commentContentToPostPostDetail, selectedPostObject.getId(), selectedPostObject.getWriter())
        }

        // Set up on click listener for the send image as comment button
        selectPictureForCommentButton.setOnClickListener {
            // The bottom sheet object (post detail send image menu)
            val bottomSheet = PostDetailSendImageMenu(this, selectedPostObject.getId())

            // Show the menu
            bottomSheet.show(supportFragmentManager, "TAG")
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

        // Add new comment to the array of comments
        arrayOfComments.add(commentObject)

        // Since this will update the view, it MUST run on the UI thread
        runOnUiThread{
            // Update the RecyclerView
            postDetailView.adapter!!.notifyDataSetChanged()
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
            postDetailView.adapter!!.notifyDataSetChanged()
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
            notificationRepository.createNotificationObjectInDatabase("commented", commentReceiverUserId, commentWriterId, photoObject.getImageURL(), selectedPostObject.getId()) { }
            notificationRepository.sendNotificationToAUser(commentReceiverUserId, "comment", "") { }
            //-------------- Image for the notification --------------

            // Emit event to the server and let the server know that new comment has been added
            MainMenu.mSocket.emit("newComment", gs.toJson(hashMapOf(
                "commentId" to "",
                "writer" to commentWriterId,
                "content" to commentContentToPostEditText.text.toString(),
                "postId" to selectedPostObject.getId()
            )))

            //-------------- Update the UI --------------
            // Create new comment object based on info of the newly created comment
            val newCommentObject = PostComment(commentContentToPostEditText.text.toString(), commentWriterId, "")

            // Add the newly created comment object to the array
            arrayOfComments.add(newCommentObject)

            // Empty content of the EditText
            commentContentToPostEditText.setText("")

            // Reload the RecyclerView
            postDetailView.adapter!!.notifyDataSetChanged()
            //-------------- End update the UI --------------
        }
    }
    //*********************************** END CREATE NEW COMMENT SEQUENCE ***********************************

    //*********************************** GET POST DETAIL SEQUENCE ***********************************
    // The function to load post info of the selected post
    private fun getPostDetail (postId: String) {
        // Call the function to get detail info of the post
        postViewModel.getPostDetail(postId) {arrayOfImages, arrayOfComments ->
            // Update adapter for the RecyclerView
            adapter = RecyclerViewAdapterCuckooPostDetail(selectedPostObject, arrayOfImages, arrayOfComments, this@PostDetail, this, executorService)

            // Add adapter to the RecyclerView
            postDetailView.adapter = adapter

            // Update array of images
            this.arrayOfImages = arrayOfImages

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

    //******************************** OPEN COMMENT OPTIONS MENU SEQUENCE ********************************
    fun openCommentOptionsMenu(commentId: String) {
        // The bottom sheet object (comment options menu)
        val bottomSheet = CommentOptionsMenu(this, commentId, executorService)

        // Show the menu
        bottomSheet.show(supportFragmentManager, "TAG")
    }
    //******************************** END OPEN COMMENT OPTIONS MENU SEQUENCE ********************************
}
