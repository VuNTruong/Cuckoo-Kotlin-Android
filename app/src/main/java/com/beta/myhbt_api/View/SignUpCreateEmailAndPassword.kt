package com.beta.myhbt_api.View

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.beta.myhbt_api.Controller.GetAllowedUserInfoBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Controller.SignUpService
import com.beta.myhbt_api.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up_create_email_and_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpCreateEmailAndPassword : AppCompatActivity() {
    // Instance of the FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    override fun onBackPressed() {
        super.onBackPressed()

        // Start the main activity again (welcome page)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        // Finish the current activity
        this.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_create_email_and_password)

        // Hide the action bar
        supportActionBar!!.hide()

        // Set on click listener for the back button
        backButtonSignUp.setOnClickListener {
            // Start the main activity again (welcome page)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Finish the current activity
            this.finish()
        }

        // Set on click listener for done sign up button
        doneRegistrationButton.setOnClickListener {
            //  Call the function to create account
            signUp()
        }
    }

    // The function to perform the sign up operation
    private fun signUp () {
        // Split full name into array
        val arrayOfFullName = fullNameField.text.toString().split(" ").toTypedArray()

        // Create the sign up service
        val signUpService : SignUpService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            SignUpService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = signUpService.signUp(enterEmailRegistration.text.toString(), enterPasswordRegistration.text.toString(),
            confirmPasswordRegistration.text.toString(), arrayOfFullName[0], arrayOfFullName[1], arrayOfFullName[arrayOfFullName.size - 1], "user", "avatar", "cover")

        // Perform the API call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // Get body of the response
                val body = response.body()

                if (body != null) {
                    // Call the function to sign the user in using FirebaseAuth
                    SignInWithFirebaseTask().execute()

                    // Go to the Welcome activity
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)

                    // Finish this activity
                    finish()
                }
            }
        })
    }

    // The AsyncTask to sign user in with FirebaseAuth and provide FirebaseAuth token in order to have access to the storage
    inner class SignInWithFirebaseTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            // Sign in with Firebase
            mAuth.signInWithEmailAndPassword("allowedusers@email.com", "AllowedUser")

            return null
        }
    }
}
