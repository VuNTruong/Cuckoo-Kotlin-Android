package com.beta.cuckoo.View.WelcomeView

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.beta.cuckoo.R
import kotlinx.android.synthetic.main.activity_sign_up_success.*

class SignUpSuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_success)

        // Hide the navigation bar
        supportActionBar!!.hide()

        // Set up on click listener for the login button
        loginButtonSignUpDone.setOnClickListener {
            // Go to the login activity
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)

            // Finish this activity
            this.finish()
        }

        // Set up on click listener for the done button
        doneButtonSignUpDone.setOnClickListener {
            // Go to the welcome activity
            val intent = Intent(applicationContext, WelcomeActivity::class.java)
            startActivity(intent)

            // Finish this activity
            this.finish()
        }
    }
}