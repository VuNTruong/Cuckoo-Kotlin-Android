package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Controller.User.UpdateUserInfoService
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Fragments.ProfileFragment
import com.beta.myhbt_api.View.UpdateAvatar
import com.beta.myhbt_api.View.UpdateCoverPhoto
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterProfilePage (userObject: User, mapOfFields: HashMap<String, Any>, activity: Activity, profileFragment: ProfileFragment): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Activity of the parent activity
    private val activity = activity

    // The profile fragment
    private val profileFragment = profileFragment

    // User object of the currently logged in user
    private val userObject = userObject

    // Maps of fields with value
    private val mapOfFields = mapOfFields

    // ViewHolder for the profile setting page header
        inner class ViewHolderProfileSettingPageHeader internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
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
        fun setUpProfileSettingItem (profileSettingItemContentParam: String, profileSettingItemIconParam: Int, fieldToUpdate: String) {
            // Load info at this row into the EditText
            enterUpdateProfileSettingItemEditText.setText(profileSettingItemContentParam)

            // Load profile setting item content into the TextView
            profileSettingItemContent.text = profileSettingItemContentParam

            // Load profile setting icon into the ImageView
            profileSettingItemIcon.setImageResource(profileSettingItemIconParam)

            // Set up event listener for the submit update button
            submitUpdateProfileSettingItemButton.setOnClickListener {
                // Call the function to update item
                mapOfFields[fieldToUpdate] = enterUpdateProfileSettingItemEditText.text.toString()

                // Execute the AsyncTask to update user info
                UpdateUserInfoTask().execute()
            }
        }
    }

    // AsyncTask for updating user info
    inner class UpdateUserInfoTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            // Create the update user info service
            val updateUserInfoService: UpdateUserInfoService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
                UpdateUserInfoService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = updateUserInfoService.updateUserInfo(
                mapOfFields["avatarURL"] as String,
                mapOfFields["coverURL"] as String,
                mapOfFields["phoneNumber"] as String,
                mapOfFields["facebook"] as String,
                mapOfFields["instagram"] as String,
                mapOfFields["twitter"] as String,
                mapOfFields["zalo"] as String,
                userObject.getId()
            )

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
                        Toast.makeText(activity, "Something is not right", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "Updated", Toast.LENGTH_SHORT).show()

                        // Update the user info again
                        profileFragment.updateUserInfo()
                    }
                }
            })

            return null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view : View

        // Base on view type to return the right view holder
        // View type 0 is for the header
        return if (viewType == 0) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_page_header, parent, false)

            // Return the view holder
            ViewHolderProfileSettingPageHeader(view)
        } // View type 1 is for the profile setting item
        else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.profile_page_item, parent, false)

            // Return the view holder
            ViewHolderProfileSettingItem(view)
        }
    }

    override fun getItemCount(): Int {
        return 8
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when (position) {
            0 -> {
                // First row should be the header
                (holder as ViewHolderProfileSettingPageHeader).setUpProfilePageHeader(userObject)
            }
            1 -> {
                // Second row will show the full name
                (holder as ViewHolderProfileSettingItem).setUpProfileSettingItem(userObject.getFullName(), R.drawable.nametagico, "fullName")
            }
            2 -> {
                // Third row will show the email
                (holder as ViewHolderProfileSettingItem).setUpProfileSettingItem(userObject.getEmail(), R.drawable.ic_email_black_24dp, "email")
            }
            3 -> {
                // Fourth row will show the phone number
                (holder as ViewHolderProfileSettingItem).setUpProfileSettingItem(userObject.getPhoneNumber(), R.drawable.ic_phone_iphone_black_24dp, "phoneNumber")
            }
            4 -> {
                // Fifth row will show the facebook id
                (holder as ViewHolderProfileSettingItem).setUpProfileSettingItem(userObject.getFacebook(), R.drawable.facebooklogo, "facebook")
            }
            5  -> {
                // Sixth row will show the instagram id
                (holder as ViewHolderProfileSettingItem).setUpProfileSettingItem(userObject.getInstagram(), R.drawable.instalogo, "instagram")
            }
            6 -> {
                // Seventh row will show show the twitter id
                (holder as ViewHolderProfileSettingItem).setUpProfileSettingItem(userObject.getTwitter(), R.drawable.twitterlogo, "twitter")
            }
            else -> {
                // Last row will show the zalo id
                (holder as ViewHolderProfileSettingItem).setUpProfileSettingItem(userObject.getZalo(), R.drawable.zalologo, "zalo")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            // First row should be the header
            0
        } else {
            // The rest will just be profile setting item
            1
        }
    }
}