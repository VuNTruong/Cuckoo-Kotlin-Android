package com.beta.cuckoo.View.MainMenu

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.beta.cuckoo.FirebaseMessagingService
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.NotificationRepositories.NotificationRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Chat.ChatMainMenu
import com.beta.cuckoo.View.Fragments.*
import com.beta.cuckoo.View.Locations.LocationMainPage
import com.beta.cuckoo.View.Locations.SearchUserAround
import com.beta.cuckoo.View.Notification.Notification
import com.beta.cuckoo.View.PostRecommend.PostRecommend
import com.beta.cuckoo.View.Posts.CreatePost
import com.beta.cuckoo.View.Posts.PostAround
import com.beta.cuckoo.View.Profile.ProfileDetail
import com.beta.cuckoo.View.Profile.ProfileSetting
import com.beta.cuckoo.View.UserInfoView.ActivitySummary
import com.beta.cuckoo.View.WelcomeView.WelcomeActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main_menu.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainMenu : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // The user repository
    private lateinit var userRepository: UserRepository

    // The notification repository
    private lateinit var notificationRepository: NotificationRepository

    // These objects are used for socket.io
    private val gson = Gson()

    // Instance of the FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    // User object of the currently logged in user
    private lateinit var currentUserObject: User

    companion object {
        lateinit var mSocket: Socket
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Instantiate user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate notification repository
        notificationRepository = NotificationRepository(executorService, applicationContext)

        // Set this thing up for the button which will be used to open the hamburger menu
        setSupportActionBar(toolbar)
        nav_view.setNavigationItemSelectedListener(this)

        // Hide the action bar
        supportActionBar!!.hide()

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

        // Set the first fragment to display to be the dashboard
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                DashboardFragment()
            ).commit()
            nav_view.setCheckedItem(R.id.nav_dashboard)
            supportActionBar!!.title = "Home page"
        }

        // Call the function to do initial set up
        setUp()
    }

    // The function to set up
    private fun setUp () {
        // Get the shared preference (memory) instance
        val memory = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()

        // Register notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification"
            val descriptionText = "Channel for the notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notification_channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

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
                } else {
                    // Handle error -> task.getException();
                }
            }

        // Call the function to load info of the currently logged in user
        getCurrentUserInfo()
    }

    //************************ DO THINGS WITH THE SOCKET.IO ************************
    // The function to set up socket.IO
    private fun setUpSocketIO () {
        // This address is to connect with the server
        //mSocket = IO.socket("http://10.0.2.2:3000")
        mSocket = IO.socket("https://myhbt-api.herokuapp.com")
        //mSocket = IO.socket("http://localhost:3000")

        // Connect to the socket
        mSocket.connect()

        // Get registration token of the user and let user join in the notification room
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { task ->
            // Registration token of the user
            val token = task.token

            // Call the function to get user id of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Call the function to update socket id of the currently logged in user
                notificationRepository.updateNotificationSocket(userObject.getId(), token) {
                    // Start Firebase Messaging service
                    val intentService = Intent(this, FirebaseMessagingService::class.java)
                    startService(intentService)
                }
            }
        }
    }
    //************************ END WORKING WITH SOCKET.IO ************************

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
            }
            R.id.nav_chat -> {
                // Go to the activity where user can chat
                val intent = Intent(applicationContext, ChatMainMenu::class.java)
                startActivity(intent)

                // Override the pending animation
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
            }
            R.id.nav_create_post -> {
                // Go to the activity where user can chat
                val intent = Intent(applicationContext, CreatePost::class.java)
                startActivity(intent)

                // Override the pending animation
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
            }
            R.id.nav_profile -> {
                // Go to the activity where user can chat
                val intent = Intent(applicationContext, ProfileSetting::class.java)
                startActivity(intent)

                // Override the pending animation
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
            }
            R.id.nav_personal_profile_page -> {
                // Go to the activity where user can see profile detail of user's own
                // The intent object
                val intent = Intent(this, ProfileDetail::class.java)

                // Update user object property of the profile detail activity
                intent.putExtra("selectedUserObject", currentUserObject)

                // Start the activity
                startActivity(intent)

                // Override the pending animation
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
            }
            R.id.nav_update_location -> {
                // Go to the activity where user can chat
                val intent = Intent(applicationContext, LocationMainPage::class.java)
                startActivity(intent)

                // Override the pending animation
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
            }
            R.id.nav_user_stats -> {
                // Go to the activity where user can chat
                val intent = Intent(applicationContext, ActivitySummary::class.java)
                startActivity(intent)

                // Override the pending animation
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
            }
            R.id.nav_recommend_album -> {
                // Go to the activity where user can chat
                val intent = Intent(applicationContext, PostRecommend::class.java)
                startActivity(intent)

                // Override the pending animation
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
            }
            R.id.nav_list_of_users_around -> {
                // Go to the activity where user can chat
                val intent = Intent(applicationContext, SearchUserAround::class.java)
                startActivity(intent)

                // Override the pending animation
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
            }
            R.id.nav_list_of_posts_around -> {
                // Go to the activity where user can chat
                val intent = Intent(applicationContext, PostAround::class.java)
                startActivity(intent)

                // Override the pending animation
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
            }
            R.id.nav_notifications -> {
                // Go to the activity where user can chat
                val intent = Intent(applicationContext, Notification::class.java)
                startActivity(intent)

                // Override the pending animation
                overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
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
        // Call the function to start signing user out
        userRepository.getInfoOfCurrentUser { userObject ->
            // Get registration token of the user delete notification socket as user signs out
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { task ->
                // Registration token of the user
                val token = task.token

                // Call the function to delete notification socket for the user
                notificationRepository.deleteNotificationSocket(userObject.getId(), token) {
                    // Sign the user out with FirebaseAuth
                    mAuth.signOut()
                    //FirebaseAuth.getInstance().signOut()

                    // Go to the main page activity
                    val intent = Intent(applicationContext, WelcomeActivity::class.java)
                    startActivity(intent)

                    // Finish this activity
                    this@MainMenu.finish()
                }
            }
        }
    }

    // The function to get user info based on id
    fun getCurrentUserInfo() {
        // Call the function to get info of the currently logged in user
        userRepository.getInfoOfCurrentUser { userObject ->
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

            // Call the function to set up socket io for the whole app
            setUpSocketIO()
        }
    }

    fun openDrawerMenu () {
        drawer_layout.openDrawer(Gravity.LEFT)
    }
}