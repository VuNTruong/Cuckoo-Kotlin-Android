package com.beta.cuckoo.View.WelcomeView

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide the action bar
        supportActionBar!!.hide()

        // Do the fade in and fade out transition to make it look better
        overridePendingTransition(R.animator.fade_in, R.animator.fade_out)

        // Set on click listener for the login button
        loginButton.setOnClickListener {
            // Start the activity where the user can sign in
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)

            // Finish the current activity
            this.finish()
        }

        // Set on click listener for the sign up button
        signUpButton.setOnClickListener {
            // Start the activity where the user can start signing up
            val intent = Intent(applicationContext, SignUp::class.java)
            startActivity(intent)

            // Finish the current activity
            this.finish()
        }
    }
}
