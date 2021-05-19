package com.beta.cuckoo.View.WelcomeView

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
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
        val intent = Intent(this, WelcomeActivity::class.java)
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

        // Hide the error message indicator
        signUpErrorMessage.visibility = View.INVISIBLE

        // Hide the processing layout
        signUpProcessingLayout.visibility = View.INVISIBLE

        // Set on click listener for the back button
        backButtonSignUp.setOnClickListener {
            // Start the main activity again (welcome page)
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)

            // Finish the current activity
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Set on click listener for done sign up button
        doneRegistrationButton.setOnClickListener {
            // Show the sign up processing layout
            signUpProcessingLayout.visibility = View.VISIBLE

            // Call the function to create account
            signUp()
        }
    }

    // The function to perform the sign up operation
    private fun signUp () {
        // Start the sign up operation
        userRepository.signUp(fullNameField.text.toString(), enterEmailRegistration.text.toString(),
            enterPasswordRegistration.text.toString(), confirmPasswordRegistration.text.toString()) {signUpSuccess, errorMessage ->
            // If sign up is successful, go to the main activity
            if (signUpSuccess) {
                // Go to the sign up done activity
                val intent = Intent(applicationContext, SignUpSuccessActivity::class.java)
                startActivity(intent)

                // Finish this activity
                finish()
            } // Otherwise, show the error
            else {
                // Hide the sign up processing layout
                signUpProcessingLayout.visibility = View.INVISIBLE

                // Show the error message indicator
                signUpErrorMessage.visibility = View.VISIBLE

                // Set the message
                signUpErrorMessage.text = errorMessage
            }
        }
    }
}
