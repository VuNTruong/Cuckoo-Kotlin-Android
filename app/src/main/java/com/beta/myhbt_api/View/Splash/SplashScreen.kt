package com.beta.myhbt_api.View.Splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.WelcomeView.MainActivity

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Hide the action bar
        supportActionBar!!.hide()

        // Go to the main activity
        android.os.Handler().postDelayed(
            {
                // Start the main menu activity
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)

                // Do the fade in and fade out transition to make it look better
                overridePendingTransition(R.animator.fade_in, R.animator.fade_out)

                // Finish the current activity so that the user won't be able to
                // come back to the splash screen
                this.finish()
            }, 3000
        )
    }
}