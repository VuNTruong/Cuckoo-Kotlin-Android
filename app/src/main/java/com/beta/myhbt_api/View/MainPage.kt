package com.beta.myhbt_api.View

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
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
}
