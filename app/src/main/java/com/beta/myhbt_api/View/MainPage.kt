package com.beta.myhbt_api.View

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.LogoutPostDataService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
import kotlinx.android.synthetic.main.activity_main_page.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        // Call the function to load info of the currently logged in user
        getCurrentUserInfo()

        // Set up on click listener for the logout button
        signOutButton.setOnClickListener {
            // Call the function to sign the user out
            logout()
        }
    }

    // The function to perform the logout operation
    private fun logout () {
        // Create the post service
        val postService: LogoutPostDataService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            LogoutPostDataService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = postService.logout()

        // Perform the API call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is null, it means that the user may didn't enter the correct email or password
                if (response.body() == null) {
                    // Show the user that the login was not successful
                    Toast.makeText(applicationContext, "Something is not right", Toast.LENGTH_SHORT).show()
                } else {
                    val responseBody = response.body()
                    print(responseBody)

                    val memory : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    val cookie = memory.getStringSet("PREF_COOKIE", HashSet<String>())
                    print(cookie)

                    // Go to the mail page activity
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        })
    }

    // The function to perform the post request and get info of the currently logged in user
    private fun getCurrentUserInfo () {
        // Create the validate token service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(GetCurrentlyLoggedInUserInfoService::class.java)

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

                    // Get name of the user
                    val firstName = data["firstName"] as String
                    val middleName = data["middleName"] as String
                    val lastName = data["lastName"] as String
                    // Combine them all to get the full name
                    val fullName = "$lastName $middleName $firstName"
                    print(fullName)
                } else {
                    print("Something is not right")
                }
            }
        })
    }
}
