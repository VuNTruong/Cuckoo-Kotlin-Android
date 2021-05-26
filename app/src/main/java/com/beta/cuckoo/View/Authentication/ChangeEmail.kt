package com.beta.cuckoo.View.Authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.AuthenticationRepositories.AuthenticationRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_change_email.*
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.activity_update_cover_photo.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.sql.StatementEvent

class ChangeEmail : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Authentication repository
    private lateinit var authenticationRepository: AuthenticationRepository

    // The user repository
    private lateinit var userRepository: UserRepository

    // Maps of fields with value
    private var mapOfFields = HashMap<String, Any>()

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

        // Instantiate the user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate authentication repository
        authenticationRepository = AuthenticationRepository(executorService, applicationContext)

        // Call the function to get basic info of the currently logged in user
        // so that it will populate map of fields which will be used to update email in MongoDB
        getCurrentUserInfo()

        // Set on click listener for the back button
        backButtonChangeEmail.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Set on click listener for the update email button
        changeEmailButton.setOnClickListener {
            // Call the function to update password
            updateEmail(enterEmailChangeEmail.text.toString(), enterPasswordChangeEmail.text.toString(), enterNewEmailField.text.toString())
        }
    }

    // The function to get info of the currently logged in user
    private fun getCurrentUserInfo () {
        // Call the function to get info of the currently logged in user
        userRepository.getInfoOfCurrentUser { userObject ->
            // Build the map of fields
            mapOfFields = hashMapOf(
                "email" to userObject.getEmail(),
                "userId" to userObject.getId(),
                "avatarURL" to userObject.getAvatarURL(),
                "coverURL" to userObject.getCoverURL()
            )
        }
    }

    // The function to update email
    private fun updateEmail (emailConfirm: String, passwordConfirm: String, newEmail: String) {
        // Call the function to update email
        authenticationRepository.updateEmail(emailConfirm, passwordConfirm, newEmail) {isUpdated, errorMessage ->
            // If the email is updated, let the user know
            if (isUpdated) {
                // Update the email in MongoDB
                mapOfFields["email"] = newEmail

                // Call the function to update user info in MongoDB
                userRepository.updateCurrentUserInfo(mapOfFields) {done ->
                    if (done) {
                        // Show toast to the user
                        Toast.makeText(applicationContext, "Email updated!", Toast.LENGTH_SHORT).show()

                        // Finish the activity
                        finish()
                    }
                }
            } // Otherwise, let the user know that there is an issue
            else {
                // Show toast to the user
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}