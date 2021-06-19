package com.beta.cuckoo.View.Locations

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.LocationRepositories.LocationRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterLocationPage
import com.beta.cuckoo.View.Profile.ProfileSetting
import kotlinx.android.synthetic.main.activity_location_main_page.*
import kotlinx.android.synthetic.main.activity_post_around.*
import kotlinx.android.synthetic.main.activity_see_friends_location.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LocationMainPage : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterLocationPage

    // Array of option contents
    private var optionContents = ArrayList<String>()

    // Array of option icons
    private var optionIcons = ArrayList<Int>()

    // Array of on click listener for the options
    private var optionOnClickListener = ArrayList<View.OnClickListener>()

    // Location repository
    private lateinit var locationRepository: LocationRepository

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_main_page)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate location repository
        locationRepository = LocationRepository(executorService, applicationContext)

        // Set on click listener for the back button
        backButtonLocationCenter.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Instantiate the recycler view
        locationPageOptionView.layoutManager = LinearLayoutManager(this)
        locationPageOptionView.itemAnimator = DefaultItemAnimator()

        // Call the function to check and see if user turns on location or not
        checkLocationSharingStatusOfCurrentUser { isTurnedOn ->
            // If user turns on location sharing, set up the location main page
            if (isTurnedOn) {
                // Show the main page and hide the turn on location layout
                locationPageOptionView.visibility = View.VISIBLE
                turnOnLocationLayoutLocationMainPage.visibility = View.INVISIBLE

                // Add content to the option content array
                optionContents.add("Cuckoo map")
                optionIcons.add(R.drawable.see_friends_location)
                optionOnClickListener.add(View.OnClickListener {
                    // Go to the activity where the user can see friends location
                    val intent = Intent(this, SeeUsersLocation::class.java)

                    // Start the see friends location activity
                    startActivity(intent)
                })

                optionContents.add("Update location")
                optionIcons.add(R.drawable.update_location)
                optionOnClickListener.add(View.OnClickListener {
                    // Go to the activity where the user can update location
                    val intent = Intent(this, UpdateLocation::class.java)

                    // Start the update location activity
                    this.startActivity(intent)
                })

                // Update the adapter
                adapter = RecyclerViewAdapterLocationPage(optionContents, optionIcons, optionOnClickListener)

                // Add adapter to the RecyclerView
                locationPageOptionView.adapter = adapter
            } // Otherwise, show layout and let the user know that user needs to turn on location sharing
            else {
                // Show the turn location layout and hide the location main page
                turnOnLocationLayoutLocationMainPage.visibility = View.VISIBLE
                locationPageOptionView.visibility = View.INVISIBLE

                // Set up on click listener for the go to settings button
                gotoAccountSettingsButtonLocationMainPage.setOnClickListener {
                    val intent = Intent(applicationContext, ProfileSetting::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    // The function to check and see if user turns on location sharing or not
    private fun checkLocationSharingStatusOfCurrentUser (callback: (isTurnedOn: Boolean) -> Unit) {
        // Call the function to check and see if user enable location or not
        locationRepository.getLocationEnableStatusOfCurrentUser { locationEnable ->
            // If location is enabled, call the callback function and return true
            if (locationEnable == "Enabled") {
                callback(true)
            } else {
                callback(false)
            }
        }
    }
}