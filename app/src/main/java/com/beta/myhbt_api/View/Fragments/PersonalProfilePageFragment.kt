package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetPhotosOfUserService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterProfileDetail
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_user_personal_profile_page.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PersonalProfilePageFragment : Fragment() {
    // Adapter for the RecyclerView
    private lateinit var adapter: RecyclerViewAdapterProfileDetail

    // User object of the user
    private var userObject = User("", "", "", "", "", "", "", "","", "", "", "", "", "", "")

    // Array of images
    private var arrayOfImages = ArrayList<HBTGramPostPhoto>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_personal_profile_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the recycler view
        userPersonalProfilePageView.layoutManager = LinearLayoutManager(this.requireActivity())
        userPersonalProfilePageView.itemAnimator = DefaultItemAnimator()

        // Call the function to load info of the current user
        getInfoOfCurrentUserAndFurtherInfo()
    }

    //******************************************* LOAD INFO OF CURRENT USER SEQUENCE *******************************************
    // The function to get id of current user which will then check if user at this activity is current or not
    private fun getInfoOfCurrentUserAndFurtherInfo () {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
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
                    // In order to prevent us from encountering the class cast exception, we need to do the following
                    // Create the GSON object
                    val gs = Gson()

                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body (user object)
                    // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                    val jsUser = gs.toJson(responseBody["data"])

                    // Convert the JSOn string back into User class
                    val userModel = gs.fromJson<User>(jsUser, User::class.java)

                    // Update the user object
                    userObject = userModel

                    // Call the function to load photos of the current user
                    loadPhotosOfUser(userModel.getId(), true)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to load all photos created by the user
    fun loadPhotosOfUser (userId: String, currentUser: Boolean) {
        // Create the get images of user service
        val getPhotosOfUserService: GetPhotosOfUserService = RetrofitClientInstance.getRetrofitInstance(this.requireActivity())!!.create(
            GetPhotosOfUserService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getPhotosOfUserService.getPhotosOfUser(userId)

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

                    // Get data from the response body (array of images created by the user)
                    val data = responseBody["data"] as ArrayList<HBTGramPostPhoto>

                    // Set the array of images be the one we just got
                    arrayOfImages = data

                    // Update the adapter
                    adapter = RecyclerViewAdapterProfileDetail(arrayOfImages, userObject, this@PersonalProfilePageFragment.requireActivity(), currentUser)

                    // Add adapter to the RecyclerView
                    userPersonalProfilePageView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //******************************************* END LOAD INFO OF CURRENT USER SEQUENCE *******************************************
}