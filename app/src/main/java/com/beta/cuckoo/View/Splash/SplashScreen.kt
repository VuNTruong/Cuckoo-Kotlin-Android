package com.beta.cuckoo.View.Splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.beta.cuckoo.R
import com.beta.cuckoo.View.VideoChat.VideoChat
import com.beta.cuckoo.View.WelcomeView.MainActivity
import com.beta.cuckoo.View.ZoomImage

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Hide the action bar
        supportActionBar!!.hide()

        // Do the fade in and fade out transition to make it look better
        overridePendingTransition(R.animator.fade_in, R.animator.fade_out)

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