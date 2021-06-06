package com.beta.cuckoo.View.Authentication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.AuthenticationRepositories.AuthenticationRepository
import kotlinx.android.synthetic.main.activity_forgot_password.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ForgotPassword : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Authentication repository
    private lateinit var authenticationRepository: AuthenticationRepository

    override fun onBackPressed() {
        super.onBackPressed()

        // Finish the current activity
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate authentication repository
        authenticationRepository = AuthenticationRepository(executorService, applicationContext)

        // Set on click listener for the back button
        backButtonForgotPassword.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Set on click listener for the send password reset email button
        sendPasswordResetEmailButton.setOnClickListener {
            // Call the function to send password reset email to specified email address
            sendPasswordResetEmail(emailToSendField.text.toString())
        }
    }

    // The function to send password reset email to the user
    private fun sendPasswordResetEmail (emailToSend: String) {
        // Call the function to send password reset email to the user
        authenticationRepository.sendPasswordResetEmail(emailToSend) {isSent, errorMessage ->
            // If email was sent, show toast to the user and let user know that password reset email was sent
            if (isSent) {
                // Show toast to the user
                Toast.makeText(applicationContext, "Password reset email sent", Toast.LENGTH_SHORT).show()
            } // If email was not sent, show toast to the user and let user know that password reset email was not sent
            else {
                // Show toast to the user
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}