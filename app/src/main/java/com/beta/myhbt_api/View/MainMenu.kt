package com.beta.myhbt_api.View

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.beta.myhbt_api.BackgroundServices
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.LogoutPostDataService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Fragments.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main_menu.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainMenu : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // These objects are used for socket.io
    private val gson = Gson()

    // Instance of the FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    // User object of the currently logged in user
    private lateinit var currentUserObject: User

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
        val intentService = Intent(this, BackgroundServices::class.java)
        startService(intentService)

        // Call the function to load info of the currently logged in user
        getCurrentUserInfo()
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
                // Go to the activity where user can see profile detail of user's own
                // The intent object
                val intent = Intent(this, ProfileDetail::class.java)

                // Update user object property of the profile detail activity
                intent.putExtra("selectedUserObject", currentUserObject)

                // Start the activity
                startActivity(intent)
            }
            R.id.nav_update_location -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    UpdateLocationFragment()
                ).commit()
                supportActionBar!!.title = "Locations"
            }
            R.id.nav_user_stats -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    UserStatsFragment()
                ).commit()
                supportActionBar!!.title = "Activity summary"
            }
            R.id.nav_recommend_album -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    RecommendAlbumFragment()
                ).commit()
                supportActionBar!!.title = "Explore"
            }
            R.id.nav_list_of_users_around -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    SearchUserAroundFragment()
                ).commit()
                supportActionBar!!.title = "Who's around?"
            }
            R.id.nav_list_of_posts_around -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    PostsAroundFragment()
                ).commit()
                supportActionBar!!.title = "What's going on around?"
            }
            R.id.nav_notifications -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    NotificationFragment()
                ).commit()
                supportActionBar!!.title = "Notifications"
            }
            R.id.nav_signout -> {
                // Call the function to sign user out
                signOut()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // The function to sign user out
    private fun signOut () {
        // Create the post service
        val postService: LogoutPostDataService = RetrofitClientInstance.getRetrofitInstance(
            applicationContext
        )!!.create(
            LogoutPostDataService::class.java
        )

        // The call object which will then be used to perform the API call
        val call: Call<Any> = postService.logout()

        // Perform the API call
        call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is null, it means that the user may didn't enter the correct email or password
                if (response.body() == null) {
                    // Show the user that the login was not successful
                    Toast.makeText(applicationContext, "Something is not right", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val memory: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                        applicationContext
                    )
                    //val cookie = memory.getStringSet("PREF_COOKIE", HashSet<String>())

                    // Sign the user out with FirebaseAuth
                    mAuth.signOut()

                    // Stop the background service
                    val intentService = Intent(this@MainMenu, BackgroundServices::class.java)
                    stopService(intentService)

                    // Go to the main page activity
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)

                    // Finish this activity
                    this@MainMenu.finish()
                }
            }
        })
    }

    // The function to get user info based on id
    fun getCurrentUserInfo() {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // Create the validate token service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(
            applicationContext
        )!!.create(
            GetCurrentlyLoggedInUserInfoService::class.java
        )

        // Create the call object in order to perform the call
        val call: Call<Any> = getCurrentlyLoggedInUserInfoService.getCurrentUserInfo()

        // Perform the call
        call.enqueue(object : Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that there is data
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Convert user object which is currently a linked tree map into a JSON string
                    val jsUser = gs.toJson(data)

                    // Convert the JSOn string back into User class
                    val userObject = gs.fromJson<User>(jsUser, User::class.java)

                    /*
                    // Load full name into the TextView
                    userFullNameDrawerMenu.text = userObject.getFullName()

                    // Load email into the TextView
                    userEmailDrawerMenu.text = userObject.getEmail()

                    // Load avatar into the ImageView
                    Glide.with(applicationContext)
                        .load(userObject.getAvatarURL())
                        .into(userAvatarDrawerMenu)

                     */

                    // Update current user object for this activity
                    currentUserObject = userObject
                } else {
                    print("Something is not right")
                }
            }
        })
    }
}
