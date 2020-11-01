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
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterProfilePage
import com.bumptech.glide.Glide
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

                        // Get id of the user
                        val userId = data["_id"] as String

                        // Get firstName of the user
                        val firstName = data["firstName"] as String

                        // Get middleName of the user
                        val middleName = data["middleName"] as String

                        // Get lastName of the user
                        val lastName = data["lastName"] as String

                        // Get email of the user
                        val email = data["email"] as String

                        // Get phone number of the user
                        val phoneNumber = data["phoneNumber"] as String

                        // Get facebook of the user
                        val facebook = data["facebook"] as String

                        // Get instagram of the user
                        val instagram = data["instagram"] as String

                        // Get twitter of the user
                        val twitter = data["twitter"] as String

                        // Get zalo of the user
                        val zalo = data["zalo"] as String

                        // Get role of the user
                        val role = data["role"] as String

                        // Get class code of the user
                        val classCode = data["classCode"] as String

                        // Get avatar URL of the user
                        val avatarURL = data["avatarURL"] as String

                        // Get cover photo URL of the user
                        val coverURL = data["coverURL"] as String

                        // Get student id of the user
                        val studentId = data["studendId"] as String

                        // Update the user object out of those info
                        currentUserObject = User(userId, firstName, middleName, lastName, email, phoneNumber, facebook, instagram, twitter, zalo, role, classCode, avatarURL, coverURL, studentId)

                        // Update the map of fields which will be used for user info update
                        mapOfFields = hashMapOf(
                            "avatarURL" to avatarURL,
                            "coverURL" to coverURL,
                            "phoneNumber" to phoneNumber,
                            "facebook" to facebook,
                            "instagram" to instagram,
                            "twitter" to twitter,
                            "zalo" to zalo
                        )

                        // Update the adapter
                        adapter = RecyclerViewAdapterProfilePage(currentUserObject, mapOfFields, this@ProfileFragment.requireActivity(), this@ProfileFragment)

                        // Add adapter to the RecyclerView
                        profileSettingView.adapter = adapter
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }
}