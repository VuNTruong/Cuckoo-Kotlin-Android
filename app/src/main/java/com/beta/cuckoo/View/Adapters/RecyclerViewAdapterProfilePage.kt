package com.beta.cuckoo.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.AuthenticationRepositories.AuthenticationRepository
import com.beta.cuckoo.Repository.LocationRepositories.LocationRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Authentication.ChangeEmail
import com.beta.cuckoo.View.Authentication.ChangePassword
import com.beta.cuckoo.View.Profile.ProfileSetting
import com.beta.cuckoo.View.UpdateUserInfo.UpdateAvatar
import com.beta.cuckoo.View.UpdateUserInfo.UpdateCoverPhoto
import com.bumptech.glide.Glide
import org.jetbrains.anko.find

class RecyclerViewAdapterProfilePage (userObject: User, mapOfFields: HashMap<String, Any>, activity: Activity, profileFragment: ProfileSetting, userRepository: UserRepository, locationRepository: LocationRepository): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Activity of the parent activity
    private val activity = activity

    // The profile fragment
    private val profileFragment = profileFragment

    // User object of the currently logged in user
    private val userObject = userObject

    // Maps of fields with value
    private val mapOfFields = mapOfFields

    // User repository
    private val userRepository = userRepository

    // Location repository
    private val locationRepository = locationRepository

    // ViewHolder for the profile setting page header
    inner class ViewHolderProfileSettingPageHeader internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val userAvatar : ImageView = itemView.findViewById(R.id.userAvatarProfileSetting)
        private val userCoverPhoto : ImageView = itemView.findViewById(R.id.userCoverPhotoProfileSetting)

        // The function to set up header of the profile page
        fun setUpProfilePageHeader (userObject: User) {
            // Load user avatar into the ImageView
            Glide.with(activity)
                .load(userObject.getAvatarURL())
                .into(userAvatar)

            // Load user cover photo into the ImageView
            Glide.with(activity)
                .load(userObject.getCoverURL())
                .into(userCoverPhoto)

            // Set on click listener for the avatar so that the avatar will take user to the activity where the user can update avatar
            userAvatar.setOnClickListener {
                // Go to the activity
                val intent = Intent(activity, UpdateAvatar::class.java)
                activity.startActivity(intent)
            }

            // Set on click listener for the cover photo so that the cover photo will take user to the activity where the user can update it
            userCoverPhoto.setOnClickListener {
                // Go to the activity
                val intent = Intent(activity, UpdateCoverPhoto::class.java)
                activity.startActivity(intent)
            }
        }
    }

    // ViewHolder for the profile setting item
    inner class ViewHolderProfileSettingItem internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val profileSettingItemIcon : ImageView = itemView.findViewById(R.id.profileSettingItemIcon)
        private val profileSettingItemContent : TextView = itemView.findViewById(R.id.profileSettingItemContent)
        private val enterUpdateProfileSettingItemEditText : EditText = itemView.findViewById(R.id.enterUpdateProfileSettingItemEditText)
        private val submitUpdateProfileSettingItemButton : ImageView = itemView.findViewById(R.id.submitUpdateProfileSettingItemButton)

        // The function to set up profile setting item
        fun setUpProfileSettingItem (profileSettingItemDescription: String, profileSettingItemContentParam: String, profileSettingItemIconParam: Int, fieldToUpdate: String) {
            // Load info at this row into the EditText
            enterUpdateProfileSettingItemEditText.setText(profileSettingItemContentParam)

            // Load profile setting item content into the TextView
            profileSettingItemContent.text = profileSettingItemDescription

            // Load profile setting icon into the ImageView
            profileSettingItemIcon.setImageResource(profileSettingItemIconParam)

            // If field to update is email or password, take user to the activity where user can change those
            // when tapped
            // Also, set them to be uneditable
            if (fieldToUpdate == "password") {
                enterUpdateProfileSettingItemEditText.setOnClickListener {
                    // Take user to te activity where user can update password
                    val intent = Intent(activity, ChangePassword::class.java)
                    activity.startActivity(intent)
                    activity.overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
                }
            }
            if (fieldToUpdate == "email") {
                enterUpdateProfileSettingItemEditText.setOnClickListener {
                    // Take user to the activity where user can update email
                    val intent = Intent(activity, ChangeEmail::class.java)
                    activity.startActivity(intent)
                    activity.overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
                }
            }

            // Set up event listener for the submit update button
            submitUpdateProfileSettingItemButton.setOnClickListener {
                // Check and see which field is going to be updated
                // If the field to update is the full name, just do it as usual as this one does not need to go through
                // authentication
                when (fieldToUpdate) {
                    "fullName" -> {
                        // Call the function to update item
                        mapOfFields[fieldToUpdate] = enterUpdateProfileSettingItemEditText.text.toString()

                        // Call the function to update user info
                        updateCurrentUserInfo()
                    } // If the field to update is password, take user to the activity where user can update password
                    "password" -> {
                        // Take user to te activity where user can update password
                        val intent = Intent(activity, ChangePassword::class.java)
                        activity.startActivity(intent)
                        activity.overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
                    } // If the field to update is email, take user to the activity where user can update email
                    "email" -> {
                        // Take user to the activity where user can update email
                        val intent = Intent(activity, ChangeEmail::class.java)
                        activity.startActivity(intent)
                        activity.overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left)
                    }
                }
            }
        }
    }

    // ViewHolder for the profile setting item
    inner class ViewHolderUpdateLocationEnable internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val locationEnableSwitch: Switch = itemView.findViewById(R.id.enableLocationSwitch)

        // The function to set up location enable switch
        fun setUpLocationEnableSwitch () {
            // Call the function to get location enable status of the currently logged in user
            locationRepository.getLocationEnableStatusOfCurrentUser { locationEnable ->
                if (locationEnable == "Enabled") {
                    locationEnableSwitch.isChecked = true
                    locationEnableSwitch.text = "On"
                }
            }

            // Set switch listener for the switch so that the switch can listen to changes
            locationEnableSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
                // If the switch is turned on, call the function to create a trust between the 2 users
                if (isChecked) {
                    // Enable location
                    locationRepository.updateLocationEnableStatusOfCurrentUser("Enabled") {isUpdated ->
                        // If location is enabled, change status of the switch to be "On"
                        if (isUpdated) {
                            locationEnableSwitch.text = "On"
                        }
                    }
                } // If the switch is turned off, call the function to remove a trust between the 2 users
                else {
                    // Disable location
                    locationRepository.updateLocationEnableStatusOfCurrentUser("Disabled") {isUpdated ->
                        // If location is disabled, change status of the switch to be "Off"
                        if (isUpdated) {
                            locationEnableSwitch.text = "Off"
                        }
                    }
                }
            })
        }
    }

    // The function to update info of the currently logged in user
    fun updateCurrentUserInfo () {
        // Call the function to update user info of the currently logged in user
        userRepository.updateCurrentUserInfo(mapOfFields) {done ->
            // If the current user info was updated, update the UI as well
            if (done) {
                // Update the user info again
                profileFragment.updateUserInfo()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view: View

        // Base on view type to return the right view holder
        // View type 0 is for the header
        return when (viewType) {
            0 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_page_header, parent, false)

                // Return the view holder
                ViewHolderProfileSettingPageHeader(view)
            } // View type 1 is for the profile setting item
            1 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_page_item, parent, false)

                // Return the view holder
                ViewHolderProfileSettingItem(view)
            } // View type 2 is for the location update switch
            else -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_page_item_update_location_enable, parent, false)

                // Return the view holder
                ViewHolderUpdateLocationEnable(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when (position) {
            0 -> {
                // First row should be the header
                (holder as ViewHolderProfileSettingPageHeader).setUpProfilePageHeader(userObject)
            }
            1 -> {
                // Second row will show the full name
                (holder as ViewHolderProfileSettingItem).setUpProfileSettingItem("Name", userObject.getFullName(), R.drawable.nametagico, "fullName")
            }
            2 -> {
                // Third row will show the email
                (holder as ViewHolderProfileSettingItem).setUpProfileSettingItem("Email", userObject.getEmail(), R.drawable.ic_email_black_24dp, "email")
            } 3 -> {
                // Third row will show the password
                (holder as ViewHolderProfileSettingItem).setUpProfileSettingItem("Password", "***********", R.drawable.ic_baseline_lock_24, "password")
            } else -> {
                // Last row will be the enable location switch
                (holder as ViewHolderUpdateLocationEnable).setUpLocationEnableSwitch()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                // First row of the RecyclerView should show the header
                0
            } // From 1 to 3 will be basic info
            in 1..3 -> {
                1
            }
            // Last row will be the update location enable switch
            else -> {
                2
            }
        }
    }
}