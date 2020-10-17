package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Fragments.CreatePostFragment

class RecyclerViewAdapterHBTGramPostPhoto (arrayOfSelectedPictures: ArrayList<Uri>, activity: Activity, createPostFragment: CreatePostFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of selected pictures
    private val arrayOfSelectedPictures = arrayOfSelectedPictures

    // Activity of the parent activity
    private val activity = activity

    // Create post fragment
    private val createPostFragment = createPostFragment

    // ViewHolder for the HBTGram post photo
    inner class ViewHolderHBTGramPostPhoto internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component of the view
        private var postPhoto: ImageView = itemView.findViewById(R.id.postPhotoView)

        // The remove button
        private var removeButton: ImageView = itemView.findViewById(R.id.removePhotoButton)

        // The function to set up selected picture
        fun setUpPicture (uri: Uri, position: Int) {
            postPhoto.setImageURI(uri)

            // Set up event for the remove button
            removeButton.setOnClickListener{
                // Call the function inside the CreatePostFragment in order to remove the photo at this position
                createPostFragment.updateImageRecyclerView(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Create the view object
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_photo_cell, parent, false)

        // Return the view holder
        return ViewHolderHBTGramPostPhoto(view)
    }

    override fun getItemCount(): Int {
        // Return the number of selected pictures
        return arrayOfSelectedPictures.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Initialize the picture
        (holder as ViewHolderHBTGramPostPhoto).setUpPicture(arrayOfSelectedPictures[position], position)
    }
}