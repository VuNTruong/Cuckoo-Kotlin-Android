package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetUserWithinARadiusService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterSearchFriend
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.android.synthetic.main.fragment_search_friends.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchUserAroundFragment : Fragment() {
    // Array of users
    private var users = ArrayList<User>()

    // User id of the currently logged in user
    private var currentUserId = ""

    // Last updated location of the user
    private lateinit var lastUpdatedLocation: LatLng

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterSearchFriend

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the recycler view
        searchFriendsView.layoutManager = LinearLayoutManager(this.requireContext())
        searchFriendsView.itemAnimator = DefaultItemAnimator()

        // Call the function to get info of the currently logged in user and load list of users around
        getInfoOfCurrentUserAndLoadListOfUsersAround()

        // Add text watcher to the search text edit
        searchFriendEditText.addTextChangedListener(textWatcher)
    }

    // The text watcher which will take action of when there is change in search text edit
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Call the function search for user based on current search query
            searchUsers(s.toString())
        }
    }

    //************************************** GET INFO OF CURRENTLY LOGGED IN USER SEQUENCE **************************************
    // The function to get last updated location of the user in order to get who is around
    private fun getInfoOfCurrentUserAndLoadListOfUsersAround () {
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
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user id of the current user
                    val userId = data["_id"] as String

                    // Update user id property of this activity
                    currentUserId = userId
                    //---------------- Get last updated location of the user ----------------
                    // Get last updated location of the current user
                    val locationObject = data["location"] as Map<String, Any>
                    val coordinatesArray = locationObject["coordinates"] as ArrayList<Double>

                    // Get the latitude
                    val latitude = coordinatesArray[1]

                    // Get the longitude
                    val longitude = coordinatesArray[0]

                    // Create the location object for the last updated location of the current user
                    val center = LatLng(latitude, longitude)

                    // Update the user last updated location property of this activity
                    lastUpdatedLocation = center
                    //---------------- End get last updated location of the user ----------------

                    // Call the function to get list of users around user's last updated location
                    searchUsers("")
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //************************************** END GET INFO OF CURRENTLY LOGGED IN USER SEQUENCE **************************************

    //************************************** GET LIST OF USERS AROUND SEQUENCE **************************************
    // The function to load user within a radius and load users based on a search query
    private fun searchUsers (searchQuery: String) {
        // Create the search user nearby service
        val searchUserService : GetUserWithinARadiusService = RetrofitClientInstance.getRetrofitInstance(this@SearchUserAroundFragment.requireActivity())!!.create(GetUserWithinARadiusService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = searchUserService.getUserWithinARadius("${lastUpdatedLocation.latitude},${lastUpdatedLocation.longitude}", 50, "km", searchQuery)

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
                    val data = responseBody["data"] as ArrayList<User>

                    // Update list of users
                    users = data

                    // Update the adapter
                    adapter = RecyclerViewAdapterSearchFriend(users, this@SearchUserAroundFragment.requireActivity())

                    // Add adapter to the RecyclerView
                    searchFriendsView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }
    //************************************** END GET LIST OF USERS AROUND SEQUENCE **************************************
}