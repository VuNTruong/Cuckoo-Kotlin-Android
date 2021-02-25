package com.beta.myhbt_api.View.WelcomeView

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.beta.myhbt_api.R
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up_create_email_and_password.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SignUp : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Instance of the FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    // The user repository
    private lateinit var userRepository: UserRepository

    override fun onBackPressed() {
        super.onBackPressed()

        // Start the main activity again (welcome page)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        // Finish the current activity
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_create_email_and_password)

        // Initiate the user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Hide the action bar
        supportActionBar!!.hide()

        // Set on click listener for the back button
        backButtonSignUp.setOnClickListener {
            // Start the main activity again (welcome page)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Finish the current activity
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Set on click listener for done sign up button
        doneRegistrationButton.setOnClickListener {
            //  Call the function to create account
            signUp()
        }
    }

    // The function to perform the sign up operation
    private fun signUp () {
        // Start the sign up operation
        userRepository.signUp(fullNameField.text.toString(), enterEmailRegistration.text.toString(), enterPasswordRegistration.text.toString(), confirmPasswordRegistration.text.toString()) {signUpSuccess ->
            // If sign up is successful, go to the main activity
            if (signUpSuccess) {
                // Call the function to sign the user in using FirebaseAuth
                SignInWithFirebaseTask().execute()

                // Go to the Welcome activity
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)

                // Finish this activity
                finish()
            }
        }
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
