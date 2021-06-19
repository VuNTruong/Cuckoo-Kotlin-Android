package com.beta.cuckoo.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.beta.cuckoo.Model.PostPhoto
import com.beta.cuckoo.R
import com.beta.cuckoo.View.ZoomImage
import com.bumptech.glide.Glide
import com.google.gson.Gson

class RecyclerViewAdapterPostPhotoUpdatePost (arrayOfPostPhotos: ArrayList<PostPhoto>, activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of photos that go with post
    private val arrayOfPostPhotos = arrayOfPostPhotos

    // Activity of parent
    private val activity = activity

    // In order to prevent us from encountering the class cast exception, we need to do the following
    // Create the GSON object
    private val gs = Gson()

    // ViewHolder for the post photo
    inner class ViewHolderPostPhoto internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val postPhoto : ImageView = itemView.findViewById(R.id.postPhotoPostDetail)

        // The function to set up post photo
        fun setUpPostPhoto (imageURL: String) {
            // Load that image into the ImageView
            Glide.with(activity)
                .load(imageURL)
                .into(postPhoto)

            // Set on click listener for the image view
            postPhoto.setOnClickListener {
                // Call the function to take user to the activity where user can zoom and see photo
                gotoZoom(imageURL)
            }
        }
    }

    //*********************************** ADDITIONAL FUNCTIONS ***********************************
    // The function which will take user to the activity where user can zoom in and out an image
    fun gotoZoom (imageURL: String) {
        if (imageURL == "") {
            return
        }

        // The intent object
        val intent = Intent(activity, ZoomImage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // Let the activity know which image to load
        intent.putExtra("imageURLToLoad", imageURL)

        // Let message id that the image belongs to to be blank
        // because the activity does not need to know about this in this case
        intent.putExtra("messageId", "")

        // Start the activity
        activity.startActivity(intent)
    }
    //*********************************** ADDITIONAL FUNCTIONS ***********************************

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Create the view object
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_detail_post_photo, parent, false)

        // Return the view holder
        return ViewHolderPostPhoto(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Convert the image object which is currently a linked tree map into a JSON string
        val jsPhoto = gs.toJson(arrayOfPostPhotos[position])

        // Convert the JSON string back into PostPhoto class
        val photoObject = gs.fromJson<PostPhoto>(jsPhoto, PostPhoto::class.java)

        // Initialize the picture
        (holder as ViewHolderPostPhoto).setUpPostPhoto(photoObject.getImageURL())
    }

    override fun getItemCount(): Int {
        // Return number of photos
        return arrayOfPostPhotos.size
    }
}