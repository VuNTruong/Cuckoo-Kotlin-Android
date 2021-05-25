package com.beta.cuckoo.View.Adapters

import android.app.Activity
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.cuckoo.R
import com.bumptech.glide.Glide

class RecyclerVIewAdapterLabelImage (arrayOfPhotosToPost: ArrayList<Uri>, arrayOfPhotoLabels: ArrayList<String>, activity: Activity) {
    // Array of photos that go with post
    private val arrayOfPhotosToPost = arrayOfPhotosToPost

    // Array of labels that go with photos
    private val arrayOfPhotoLabels = arrayOfPhotoLabels

    // Parent activity
    private val activity = activity

    // ViewHolder for the post photos
    inner class ViewHolderCuckooPostPhotos internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val postPhoto : ImageView = itemView.findViewById(R.id.postPhotoPostDetail)

        // The function to set up post photo
        fun setUpPostPhoto (imageUri: Uri) {
            // Load image into the image view
            postPhoto.setImageURI(imageUri)
        }
    }

    // ViewHolder for the post photo labels
    inner class ViewHolderCuckooPostPhotoLabel internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val postPhotoLabel : TextView = itemView.findViewById(R.id.photoLabelToChose)
        private val removePhotoLabelButton : ImageView = itemView.findViewById(R.id.removeLabelButton)

        // The function to set up post photo label row
        fun setUpPostPhotoLabelRow (photoLabel: String, position: Int) {
            // Load photo label into the text view
            postPhotoLabel.text = photoLabel

            // Set on click listener for the remove label button
            removePhotoLabelButton.setOnClickListener {

            }
        }
    }
}