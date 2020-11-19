package com.beta.myhbt_api.View

import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.LogoutPostDataService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Fragments.*
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.nav_header.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainMenu : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // Instance of the FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Set this thing up for the button which will be used to open the hamburger menu
        setSupportActionBar(toolbar)
        nav_view.setNavigationItemSelectedListener(this)

        // To open the hamburger menu
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        // Set the toggle so that user can open and close this menu
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // Set the icon for the button
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)// set drawable icon
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Check the previous activity to see what it is. If the previous activity is the profile detail page,
        // load the profile page instead of the dashboard
        if (intent.getStringExtra("previousActivityName") == "profileDetailPage") {
            // Hide the action bar
            supportActionBar!!.hide()

            // Load the profile page
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    ProfileFragment()
                ).commit()
                nav_view.setCheckedItem(R.id.nav_dashboard)
                supportActionBar!!.title = "Profile Settings"
            }
        } // Otherwise, load the dashboard
        else {
            // Set the first fragment to display to be the dashboard
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    DashboardFragment()
                ).commit()
                nav_view.setCheckedItem(R.id.nav_dashboard)
                supportActionBar!!.title = "Home page"
            }
        }

        // Call the function to do initial set up
        setUp()
    }

    // The function to set up
    private fun setUp () {
        // Execute the AsyncTask to load full name, email and avatar for the currently logged in user
        GetCurrentUserInfoTask().execute(hashMapOf(
            "fullNameTextView" to userFullNameDrawerMenu,
            "emailTextView" to userEmailDrawerMenu,
            "avatarImageView" to userAvatarDrawerMenu
        ))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    DashboardFragment()
                ).commit()
                supportActionBar!!.title = "Dashboard"
            }
            R.id.nav_chat -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    ChatFragment()
                ).commit()
                supportActionBar!!.title = "Chat"
            }
            R.id.nav_create_post -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    CreatePostFragment()
                ).commit()
                supportActionBar!!.title = "Create new post"
            }
            R.id.nav_profile -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    ProfileFragment()
                ).commit()
                supportActionBar!!.title = "Profile Settings"
            }
            R.id.nav_search_friend -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    SearchFriendFragment()
                ).commit()
                supportActionBar!!.title = "Search friends"
            }
            R.id.nav_personal_profile_page -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    PersonalProfilePageFragment()
                ).commit()
                supportActionBar!!.title = "My personal profile"
            }
            R.id.nav_signout -> {
                // Execute the AsyncTask to sign the user out
                SignOutTask().execute()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // AsyncTask to sign the user out
    inner class SignOutTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            // Create the post service
            val postService: LogoutPostDataService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
                LogoutPostDataService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = postService.logout()

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is null, it means that the user may didn't enter the correct email or password
                    if (response.body() == null) {
                        // Show the user that the login was not successful
                        Toast.makeText(applicationContext, "Something is not right", Toast.LENGTH_SHORT).show()
                    } else {
                        val responseBody = response.body()
                        print(responseBody)

                        val memory : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        val cookie = memory.getStringSet("PREF_COOKIE", HashSet<String>())
                        print(cookie)

                        // Sign the user out with FirebaseAuth
                        mAuth.signOut()

                        // Go to the mail page activity
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            })

            return null
        }
    }

    // AsyncTask to load full name and avatar of the currently logged in user
    inner class GetCurrentUserInfoTask : AsyncTask<HashMap<String, Any>, Void, Void>() {
        override fun doInBackground(vararg params: HashMap<String, Any>?): Void? {
            // Create the validate token service
            val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
                GetCurrentlyLoggedInUserInfoService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getCurrentlyLoggedInUserInfoService.getCurrentUserInfo()

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        val body = response.body()
                        print(body)
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get avatar URL of the user
                        val avatarURL = data["avatarURL"] as String

                        // Get email of the user
                        val email = data["email"] as String

                        // Get name of the user
                        val firstName = data["firstName"] as String
                        val middleName = data["middleName"] as String
                        val lastName = data["lastName"] as String
                        // Combine them all to get the full name
                        val fullName = "$lastName $middleName $firstName"

                        // Load full name into the TextView
                        userFullNameDrawerMenu.text = fullName

                        // Load email into the TextView
                        userEmailDrawerMenu.text = email

                        // Load avatar into the ImageView
                        Glide.with(applicationContext)
                            .load(avatarURL)
                            .into(userAvatarDrawerMenu)
                    } else {
                        print("Something is not right")
                    }
                }
            })

            return null
        }
    }
}
