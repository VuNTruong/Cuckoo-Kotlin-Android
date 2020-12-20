package com.beta.myhbt_api.View.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterLocationPage
import com.beta.myhbt_api.View.SeeFriendsLocation
import com.beta.myhbt_api.View.UpdateLocation
import kotlinx.android.synthetic.main.fragment_update_location.*

class UpdateLocationFragment : Fragment() {
    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterLocationPage

    // Array of option contents
    private var optionContents = ArrayList<String>()

    // Array of option icons
    private var optionIcons = ArrayList<Int>()

    // Array of on click listener for the options
    private var optionOnClickListener = ArrayList<View.OnClickListener>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_update_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the recycler view
        locationPageOptionView.layoutManager = LinearLayoutManager(this.requireActivity())
        locationPageOptionView.itemAnimator = DefaultItemAnimator()

        // Add content to the option content array
        optionContents.add("See friend's locations")
        optionIcons.add(R.drawable.see_friends_location)
        optionOnClickListener.add(View.OnClickListener {
            // Go to the activity where the user can see friends location
            val intent = Intent(this.requireActivity(), SeeFriendsLocation::class.java)

            // Start the see friends location activity
            this.requireActivity().startActivity(intent)
        })

        optionContents.add("Update location")
        optionIcons.add(R.drawable.update_location)
        optionOnClickListener.add(View.OnClickListener {
            // Go to the activity where the user can update location
            val intent = Intent(this.requireActivity(), UpdateLocation::class.java)

            // Start the update location activity
            this.requireActivity().startActivity(intent)
        })

        // Update the adapter
        adapter = RecyclerViewAdapterLocationPage(optionContents, optionIcons, optionOnClickListener)

        // Add adapter to the RecyclerView
        locationPageOptionView.adapter = adapter
    }
}