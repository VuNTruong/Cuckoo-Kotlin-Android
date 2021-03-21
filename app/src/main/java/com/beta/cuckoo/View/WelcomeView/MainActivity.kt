package com.beta.cuckoo.View.WelcomeView

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.beta.cuckoo.View.VideoChat.VideoChat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Instance of the FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    // The user repository
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initiate the user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Set on click listener for the login button
        loginButton.setOnClickListener {
            // Start the activity where the user can sign in
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)

            // Finish the current activity
            this.finish()
        }

        // Show the loading layout at beginning and the main layout to be invisible
        loadingLayoutWelcome.visibility = View.VISIBLE
        welcomeLayout.visibility = View.INVISIBLE

        // Set on click listener for the sign up button
        signUpButton.setOnClickListener {
            // Start the activity where the user can start signing up
            val intent = Intent(applicationContext, SignUp::class.java)
            startActivity(intent)

            // Finish the current activity
            this.finish()
        }

        // Go to the main activity
        val intent = Intent(applicationContext, VideoChat::class.java)
        startActivity(intent)
        // Call the function to validate the current token
        //checkToken()
    }

    // The function to check if current token is still valid or not
    private fun checkToken () {
        mAuth.getAccessToken(true)
            .addOnCompleteListener{task ->
                if (task.isSuccessful) {
                    val idToken = task.result.token
                    print(idToken)
                }
            }

        // Call the function to validate the login token of the currently logged in user
        userRepository.checkToken { isValid ->
            // If the response body is not empty it means that the token is valid
            if (isValid) {
                // Go to the main activity
                val intent = Intent(applicationContext, VideoChat::class.java)
                startActivity(intent)

                // Pass name of this activity to the main menu so that it will know to load the dashboard
                intent.putExtra("previousActivityName", "mainActivity")

                // Finish this activity
                this@MainActivity.finish()
            } else {
                // Show the welcome layout and the loading layout to be invisible
                loadingLayoutWelcome.visibility = View.INVISIBLE
                welcomeLayout.visibility = View.VISIBLE
            }
        }
    }
}
