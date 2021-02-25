package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterProfilePage
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ProfileFragment : Fragment() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // User repository
    private lateinit var userRepository: UserRepository

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterProfilePage?= null

    // User object of the currently logged in user
    private var currentUserObject = User("", "", "", "", "", "", "", "", "", "", "", "", "")

    // Maps of fields with value
    private var mapOfFields = HashMap<String, Any>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate user repository
        userRepository = UserRepository(executorService, this.requireContext())

        // Instantiate the recycler view
        profileSettingView.layoutManager = LinearLayoutManager(this@ProfileFragment.context)
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
        // Call the function to get info of the currently logged in user
        userRepository.getInfoOfCurrentUser { userObject ->
            // Update the user object out of those info
            currentUserObject = userObject

            // Update the map of fields which will be used for user info update
            mapOfFields = hashMapOf(
                "avatarURL" to userObject.getAvatarURL(),
                "coverURL" to userObject.getCoverURL(),
                "phoneNumber" to userObject.getPhoneNumber(),
                "facebook" to userObject.getFacebook(),
                "instagram" to userObject.getInstagram(),
                "twitter" to userObject.getTwitter(),
                "zalo" to userObject.getZalo()
            )

            // Update the adapter
            adapter = RecyclerViewAdapterProfilePage(currentUserObject, mapOfFields, this@ProfileFragment.requireActivity(), this@ProfileFragment, userRepository)

            // Add adapter to the RecyclerView
            profileSettingView.adapter = adapter

            // Show the user layout and hide the loading layout
            profileSettingView.visibility = View.VISIBLE
            loadingLayoutProfileSetting.visibility = View.INVISIBLE
        }
    }
}