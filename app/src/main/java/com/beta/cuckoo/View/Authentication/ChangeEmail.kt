package com.beta.cuckoo.View.Authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.AuthenticationRepositories.AuthenticationRepository
import kotlinx.android.synthetic.main.activity_change_email.*
import kotlinx.android.synthetic.main.activity_change_password.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChangeEmail : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Authentication repository
    private lateinit var authenticationRepository: AuthenticationRepository

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate authentication repository
        authenticationRepository = AuthenticationRepository(executorService, applicationContext)

        // Set on click listener for the back button
        backButtonChangeEmail.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Set on click listener for the update email button
        changeEmailButton.setOnClickListener {
            // Call the function to update password
            updateEmail(enterNewEmailField.text.toString())
        }
    }

    // The function to update email
    private fun updateEmail (newEmail: String) {
        // Call the function to update email
        authenticationRepository.updateEmail(newEmail) {isUpdated, errorMessage ->
            // If the email is updated, let the user know
            if (isUpdated) {
                // Show toast to the user
                Toast.makeText(applicationContext, "Email updated!", Toast.LENGTH_SHORT).show()

                // Finish the activity
                finish()
            } // Otherwise, let the user know that there is an issue
            else {
                // Show toast to the user
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}