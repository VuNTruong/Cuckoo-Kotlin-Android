package com.beta.cuckoo.View.UserInfoView

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Interfaces.CreateNotificationInterface
import com.beta.cuckoo.Model.PostPhoto
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.MessageRepositories.MessageRepository
import com.beta.cuckoo.Repository.NotificationRepositories.NotificationRepository
import com.beta.cuckoo.Repository.PostRepositories.PostRepository
import com.beta.cuckoo.Repository.UserRepositories.FollowRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.Repository.UserStatsRepositories.UserStatsRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterProfileDetail
import com.beta.cuckoo.ViewModel.PhotoViewModel
import kotlinx.android.synthetic.main.activity_profile_detail.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ProfileDetail : AppCompatActivity(), CreateNotificationInterface {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Notification repository
    private lateinit var notificationRepository: NotificationRepository

    // User stats repository
    private lateinit var userStatsRepository: UserStatsRepository

    // User repository
    private lateinit var userRepository: UserRepository

    // Profile repository
    private lateinit var postRepository: PostRepository

    // Message repository
    private lateinit var messageRepository: MessageRepository

    // Follow repository
    private lateinit var followRepository: FollowRepository

    // Photo view model
    private lateinit var photoViewModel: PhotoViewModel

    // Adapter for the RecyclerView
    private lateinit var adapter: RecyclerViewAdapterProfileDetail

    // User object of the user
    private var userObject = User("", "", "", "", "", "", "", "","", "", "", "", "")

    // Array of images
    private var arrayOfImages = ArrayList<PostPhoto>()

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate user stats repository
        userStatsRepository = UserStatsRepository(executorService, applicationContext)

        // Instantiate user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate notification repository
        notificationRepository = NotificationRepository(executorService, applicationContext)

        // Instantiate post repository
        postRepository = PostRepository(executorService, applicationContext)

        // Instantiate message repository
        messageRepository = MessageRepository(executorService, applicationContext)

        // Instantiate follow repository
        followRepository = FollowRepository(executorService, applicationContext)

        // Instantiate photo view model
        photoViewModel = PhotoViewModel(applicationContext)

        // Set on click listener for the back button
        backButtonProfileDetail.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Get selected user object from the previous activity
        userObject = intent.getSerializableExtra("selectedUserObject") as User

        // Instantiate the recycler view
        profileDetailView.layoutManager = LinearLayoutManager(applicationContext)
        profileDetailView.itemAnimator = DefaultItemAnimator()

        // Call the function to load further info of the user
        getInfoOfCurrentUserAndFurtherInfo()

        // Call the function to update user profile visit
        updateUserProfileVisit()
    }

    //******************************************* LOAD INFO OF USER SEQUENCE *******************************************
    // The function to get id of current user which will then check if user at this activity is current or not
    private fun getInfoOfCurrentUserAndFurtherInfo () {
        // Call the function to get info of the currently logged in user
        userRepository.getInfoOfCurrentUser { userObjectParam ->
            // Check to see if user object at this activity is the current user or not, then call
            // the function to set up rest of the view
            if (userObjectParam.getId() == userObject.getId()) {
                // Call the function to set up the rest as well as let the function know that user at this activity is the current user
                loadPhotosOfUser(userObject.getId(), true)
            } // Otherwise, call the function to set up the rest and let it know that user at this activity is not the current user
            else {
                loadPhotosOfUser(userObject.getId(), false)
            }
        }
    }

    // The function to load all photos created by the user
    private fun loadPhotosOfUser (userId: String, currentUser: Boolean) {
        // Call the function to get photos created by the user
        photoViewModel.loadPhotosCreatedByUserWithSpecifiedId(userId) {arrayOfImagesByUser ->
            // Set the array of images be the one we just got
            arrayOfImages = arrayOfImagesByUser

            // Update the adapter
            adapter = RecyclerViewAdapterProfileDetail(arrayOfImages, userObject, this@ProfileDetail, currentUser, userRepository, postRepository, messageRepository, followRepository)

            // Add adapter to the RecyclerView
            profileDetailView.adapter = adapter
        }
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
        // Call the function to update user profile visit
        userStatsRepository.updateUserProfileVisitFromCurrentUser(userObject.getId())
    }
    //******************************************* END UPDATE PROFILE VISIT SEQUENCE *******************************************

    //******************************** CREATE NOTIFICATION SEQUENCE ********************************
    // The function to create new notification
    override fun createNotification (content: String, forUser: String, fromUser: String, image: String, postId: String) {
        // Call the function to create notification
        notificationRepository.sendNotification(content, forUser, fromUser, image, postId)
    }
    //******************************** END CREATE NOTIFICATION SEQUENCE ********************************
}
