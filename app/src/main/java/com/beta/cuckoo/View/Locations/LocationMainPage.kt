package com.beta.cuckoo.View.Locations

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.R
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterLocationPage
import kotlinx.android.synthetic.main.activity_location_main_page.*
import kotlinx.android.synthetic.main.activity_see_friends_location.*

class LocationMainPage : AppCompatActivity() {
    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterLocationPage

    // Array of option contents
    private var optionContents = ArrayList<String>()

    // Array of option icons
    private var optionIcons = ArrayList<Int>()

    // Array of on click listener for the options
    private var optionOnClickListener = ArrayList<View.OnClickListener>()

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

        // Set on click listener for the back button
        backButtonLocationCenter.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Instantiate the recycler view
        locationPageOptionView.layoutManager = LinearLayoutManager(this)
        locationPageOptionView.itemAnimator = DefaultItemAnimator()

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
    }
}