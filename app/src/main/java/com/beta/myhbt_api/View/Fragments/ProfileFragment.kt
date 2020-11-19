package com.beta.myhbt_api.View.Fragments

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterProfilePage
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterProfilePage?= null

    // User object of the currently logged in user
    private var currentUserObject = User("", "", "", "", "", "", "", "", "", "", "", "", "", "", "")

    // Maps of fields with value
    private var mapOfFields = HashMap<String, Any>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the recycler view
        profileSettingView.layoutManager = LinearLayoutManager(this@ProfileFragment.context)
        profileSettingView.itemAnimator = DefaultItemAnimator()

        // Hide the profile setting view initially and show the loading layout
        profileSettingView.visibility = View.INVISIBLE
        loadingLayoutProfileSetting.visibility = View.VISIBLE

        // Execute the AsyncTask to get info of the currently logged in user and create the page
        GetCurrentUserInfoTask().execute()
    }

    // The function to get user info again
    fun updateUserInfo () {
        // Execute the AsyncTask to get user info again
        GetCurrentUserInfoTask().execute()
    }

    // AsyncTask to load info of the currently logged in user
    inner class GetCurrentUserInfoTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void): Void? {
            // Create the validate token service
            val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(this@ProfileFragment.requireActivity())!!.create(
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

                        // In order to prevent us from encountering the class cast exception, we need to do the following
                        // Create the GSON object
                        val gs = Gson()

                        // Convert a linked tree map into a JSON string
                        val jsUser = gs.toJson(data)

                        // Convert the JSOn string back into User class
                        val userModel = gs.fromJson<User>(jsUser, User::class.java)

                        // Update the user object out of those info
                        currentUserObject = userModel

                        // Update the map of fields which will be used for user info update
                        mapOfFields = hashMapOf(
                            "avatarURL" to userModel.getAvatarURL(),
                            "coverURL" to userModel.getCoverURL(),
                            "phoneNumber" to userModel.getPhoneNumber(),
                            "facebook" to userModel.getFacebook(),
                            "instagram" to userModel.getInstagram(),
                            "twitter" to userModel.getTwitter(),
                            "zalo" to userModel.getZalo()
                        )

                        // Update the adapter
                        adapter = RecyclerViewAdapterProfilePage(currentUserObject, mapOfFields, this@ProfileFragment.requireActivity(), this@ProfileFragment)

                        // Add adapter to the RecyclerView
                        profileSettingView.adapter = adapter

                        // Show the user layout and hide the loading layout
                        profileSettingView.visibility = View.VISIBLE
                        loadingLayoutProfileSetting.visibility = View.INVISIBLE
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }
}