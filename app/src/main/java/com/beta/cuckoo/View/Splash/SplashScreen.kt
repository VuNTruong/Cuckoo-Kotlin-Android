package com.beta.cuckoo.View.Splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import com.beta.cuckoo.R
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.beta.cuckoo.View.WelcomeView.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {
    // Instance of the FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Hide the action bar
        supportActionBar!!.hide()

        // Hide the splash loading layout
        splashLoadingLayout.visibility = View.INVISIBLE

        // Do the fade in and fade out transition to make it look better
        overridePendingTransition(R.animator.fade_in, R.animator.fade_out)

        // Go to the main activity
        android.os.Handler().postDelayed(
            {
                // Show the splash loading layout
                splashLoadingLayout.visibility = View.VISIBLE

                // Call the function to check for user's token to see if it's valid or not
                checkToken()
            }, 3000
        )
    }

    // The function to check if current token is still valid or not
    private fun checkToken () {
        // Go to the main activity
        val intent = Intent(applicationContext, MainMenu::class.java)
        startActivity(intent)

        // Do the fade in and fade out transition to make it look better
        overridePendingTransition(R.animator.fade_in, R.animator.fade_out)

        // Finish this activity
        this.finish()

        // Get the shared preference (memory) instance
        val memory = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()

        // Check to see if there is a current user object or not
        if (mAuth.currentUser != null) {
            // Get Firebase Auth token of the user
            val mUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
            mUser!!.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Get id token
                        val idToken = task.result.token

                        // Save id token to the memory
                        memory.putString("idToken", idToken)
                        memory.apply()

                        // Go to the main activity
                        val intent = Intent(applicationContext, MainMenu::class.java)
                        startActivity(intent)

                        // Do the fade in and fade out transition to make it look better
                        overridePendingTransition(R.animator.fade_in, R.animator.fade_out)

                        // Finish this activity
                        this.finish()
                    } else {
                        // Handle error -> task.getException();
                    }
                }
        } else {
            // Go to the welcome activity
            val intent = Intent(applicationContext, WelcomeActivity::class.java)
            startActivity(intent)

            // Do the fade in and fade out transition to make it look better
            overridePendingTransition(R.animator.fade_in, R.animator.fade_out)
        }
    }
}