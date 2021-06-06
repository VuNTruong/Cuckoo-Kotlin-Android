package com.beta.cuckoo.View.Authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.AuthenticationRepositories.AuthenticationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_change_password.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChangePassword : AppCompatActivity() {
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
        setContentView(R.layout.activity_change_password)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate authentication repository
        authenticationRepository = AuthenticationRepository(executorService, applicationContext)

        // Set on click listener for the back button
        backButtonChangePassword.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Set on click listener for the update password button
        changePasswordButton.setOnClickListener {
            // Call the function to update password
            updatePassword(enterEmailChangePassword.text.toString(), enterPasswordChangePassword.text.toString(), enterNewPasswordField.text.toString(), confirmNewPassword.text.toString())
        }
    }

    // The function to update password
    private fun updatePassword (emailConfirmChangePassword: String, passwordConfirmChangePassword: String, newPassword: String, passwordConfirm: String) {
        // Call the function to update password
        authenticationRepository.updatePassword(emailConfirmChangePassword, passwordConfirmChangePassword, newPassword, passwordConfirm) {isUpdated, errorMessage ->
            // If the password is updated, let the user know
            if (isUpdated) {
                // Show toast to the user
                Toast.makeText(applicationContext, "Password updated!", Toast.LENGTH_SHORT).show()

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