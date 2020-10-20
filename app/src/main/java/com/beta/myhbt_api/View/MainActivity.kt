package com.beta.myhbt_api.View

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.beta.myhbt_api.Controller.*
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set on click listener for the login button
        loginButton.setOnClickListener {
            // Start the activity where the user can sign in
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }

        // Show the loading layout at beginning and the main layout to be invisible
        loadingLayoutWelcome.visibility = View.VISIBLE
        welcomeLayout.visibility = View.INVISIBLE

        // Set on click listener for the sign up button
        signUpButton.setOnClickListener {
            // Start the activity where the user can start signing up
            val intent = Intent(applicationContext, SignUpEnterStudentId::class.java)
            startActivity(intent)
        }

        // Call the function to validate the current token
        checkToken()
    }

    // The function to check if current token is still valid or not
    private fun checkToken () {
        // Create the validate token service
        val validateTokenService: ValidateTokenPostService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(ValidateTokenPostService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = validateTokenService.validate()

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    // Go to the main activity
                    val intent = Intent(applicationContext, MainMenu::class.java)
                    startActivity(intent)

                    // Finish this activity
                    this@MainActivity.finish()
                } else {
                    // Show the welcome layout and the loading layout to be invisible
                    loadingLayoutWelcome.visibility = View.INVISIBLE
                    welcomeLayout.visibility = View.VISIBLE
                }
            }
        })
    }

    // The function to perform the get request
    private fun performGetRequest () {
        // Create the get service
        val service: GetDataService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(GetDataService::class.java)

        // Perform the get request
        val call1: Call<Any> = service.getAllUsersMap()
        call1.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                val responseBody = response.body() as Map<String, Any>
                val arrayOfUsers = responseBody["documents"] as List<User>
                print(arrayOfUsers)
            }
        })
    }
}
