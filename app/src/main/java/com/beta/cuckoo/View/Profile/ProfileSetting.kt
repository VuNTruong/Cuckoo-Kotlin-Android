package com.beta.cuckoo.View.Profile

import android.accounts.AuthenticatorException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.AuthenticationRepositories.AuthenticationRepository
import com.beta.cuckoo.Repository.LocationRepositories.LocationRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterProfilePage
import kotlinx.android.synthetic.main.activity_profile_setting.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ProfileSetting : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // User repository
    private lateinit var userRepository: UserRepository

    // Location repository
    private lateinit var locationRepository: LocationRepository

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterProfilePage?= null

    // User object of the currently logged in user
    private var currentUserObject = User("", "", "", "", "", "")

    // Maps of fields with value
    private var mapOfFields = HashMap<String, Any>()

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setting)

        // Hide the navigation bar
        supportActionBar!!.hide()

        // Set up on click listener for the back button
        backButtonProfileSetting.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Instantiate user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate location repository
        locationRepository = LocationRepository(executorService, applicationContext)

        // Instantiate the recycler view
        profileSettingView.layoutManager = LinearLayoutManager(applicationContext)
        profileSettingView.itemAnimator = DefaultItemAnimator()

        // Hide the profile setting view initially and show the loading layout
        profileSettingView.visibility = View.INVISIBLE
        loadingLayoutProfileSetting.visibility = View.VISIBLE

        // Call the function to get info of the currently logged in user
        getCurrentUserInfo()
    }

    // The function to get user info again
    fun updateUserInfo () {
        // Call the function to get info of the currently logged in user again
        getCurrentUserInfo()
    }

    // THe function to get info of the currently logged in user
    fun getCurrentUserInfo () {
        // Update the adapter
        adapter = RecyclerViewAdapterProfilePage(currentUserObject, mapOfFields, this, this, userRepository, locationRepository)

        // Add adapter to the RecyclerView
        profileSettingView.adapter = adapter

        // Show the user layout and hide the loading layout
        profileSettingView.visibility = View.VISIBLE
        loadingLayoutProfileSetting.visibility = View.INVISIBLE

        // Call the function to get info of the currently logged in user
        userRepository.getInfoOfCurrentUser { userObject ->
            // Update the user object out of those info
            //currentUserObject = userObject

            // Update the map of fields which will be used for user info update
            mapOfFields = hashMapOf(
                "fullName" to userObject.getFullName(),
                "email" to userObject.getEmail(),
                "userId" to userObject.getId(),
                "avatarURL" to userObject.getAvatarURL(),
                "coverURL" to userObject.getCoverURL()
            )

            // Update the adapter
            adapter = RecyclerViewAdapterProfilePage(userObject, mapOfFields, this, this, userRepository, locationRepository)

            // Add adapter to the RecyclerView
            profileSettingView.adapter = adapter

            // Show the user layout and hide the loading layout
            profileSettingView.visibility = View.VISIBLE
            loadingLayoutProfileSetting.visibility = View.INVISIBLE
        }
    }
}