package com.beta.myhbt_api.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Controller.SearchUserService
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterSearchUserToChatWith
import kotlinx.android.synthetic.main.activity_search_user_to_chat_with.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchUserToChatWith : AppCompatActivity() {
    // Array of users
    private var users = ArrayList<User>()

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterSearchUserToChatWith

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user_to_chat_with)

        // Instantiate the recycler view
        searchUserToChatView.layoutManager = LinearLayoutManager(applicationContext)
        searchUserToChatView.itemAnimator = DefaultItemAnimator()

        // Add text watcher to the search text edit
        searchUserToChatEditText.addTextChangedListener(textWatcher)
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

    // The function to load list of users based on search query
    fun searchUsers (searchQuery: String) {
        // Create the search user service
        val searchUserService : SearchUserService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(SearchUserService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = searchUserService.searchUser(searchQuery)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is no error
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as ArrayList<User>

                    // Update list of users
                    users = data

                    // Update the adapter
                    adapter = RecyclerViewAdapterSearchUserToChatWith(users, this@SearchUserToChatWith)

                    // Add adapter to the RecyclerView
                    searchUserToChatView.adapter = adapter
                }
            }
        })
    }
}
