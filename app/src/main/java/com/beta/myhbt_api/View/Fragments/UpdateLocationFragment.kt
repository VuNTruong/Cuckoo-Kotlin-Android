package com.beta.myhbt_api.View.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.SeeFriendsLocation
import com.beta.myhbt_api.View.UpdateLocation
import kotlinx.android.synthetic.main.fragment_update_location.*

class UpdateLocationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_update_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle on click listener for the update location button
        shareYourLocationButton.setOnClickListener{
            // Go to the activity where the user can update location
            val intent = Intent(this.requireActivity(), UpdateLocation::class.java)

            // Start the update location activity
            this.requireActivity().startActivity(intent)
        }

        // Handle on click listener for the see friends location button
        seeFriendsLocationButton.setOnClickListener {
            // Go to the activity where the user can see friends location
            val intent = Intent(this.requireActivity(), SeeFriendsLocation::class.java)

            // Start the see friends location activity
            this.requireActivity().startActivity(intent)
        }
    }
}