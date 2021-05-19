package com.beta.cuckoo.View.Posts

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.PostRepositories.CreatePostRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterPostPhoto
import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.android.synthetic.main.activity_notification.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CreatePost : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Post repository
    private lateinit var createPostRepository: CreatePostRepository

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterPostPhoto?= null

    // Array of selected images for the post
    private val selectedImages = ArrayList<Uri>()

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        // Hide the navigation bar
        supportActionBar!!.hide()

        // Set up on click listener for the back button
        backButtonCreatePost.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Instantiate the post repository
        createPostRepository = CreatePostRepository(executorService, applicationContext)

        // Initialize the RecyclerView
        photoOfPostToCreate.layoutManager = LinearLayoutManager(this)
        photoOfPostToCreate.itemAnimator = DefaultItemAnimator()

        // Update the adapter
        adapter = RecyclerViewAdapterPostPhoto(selectedImages, this, this)

        // Add adapter to the RecyclerView
        photoOfPostToCreate.adapter = adapter

        // Add on click listener for the choose photo button
        choosePhotoButton.setOnClickListener {
            // Call the function to open the file choose
            fileChooser()
        }

        // Set on click listener for the create post button
        createPostButton.setOnClickListener {
            // Call the function to get info of the current user and create new post based on it
            createPost(postContentToCreate.text.toString(), selectedImages.size)
        }
    }

    //******************************* CHOOSE IMAGE SEQUENCE *******************************
    /*
    In this sequence, we will do 2 things
    1. Let user choose image from file
    2. Load image into the list of chosen images
     */

    // The function to open file chooser to get the image
    private fun fileChooser() {
        // Create the new intent in order to come to the file chooser
        val intent = Intent()

        // Set the intent to just pick the image
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        // Star the activity to get the image
        startActivityForResult(intent, 0)
    }

    // The function to load newly picked image into the array of selected images
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Add the selected image's Uri into the array of selected images
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImages.add(data.data!!)
        }

        // Reload the RecyclerView
        photoOfPostToCreate.adapter!!.notifyDataSetChanged()
    }

    // We also have a function here which will help removing image from list of chosen images in the list
    // The function to update the RecyclerView after an image is removed from the list
    fun updateImageRecyclerView (position: Int) {
        // Remove the image at the specified position
        selectedImages.removeAt(position)

        // Update the RecyclerView
        photoOfPostToCreate.adapter!!.notifyDataSetChanged()
    }
    //******************************* END CHOOSE IMAGE SEQUENCE *******************************

    //******************************* CREATE POST SEQUENCE *******************************
    /*
    In this sequence, we will do 5 things
    1. Get info of the currently logged in user
    2. Create new post object in the post collection of the database
    3. Upload images of the post which is already in the array of chosen images
    4. Get download URL of uploaded images and add them to the image URL collection of the database
    5. Run the image labeler to get labels that is related to the image and add them to the image label collection of the database
     */

    // The function to start creating post
    private fun createPost (postContent: String, numOfImages: Int) {
        // Call the function to start creating post
        createPostRepository.createNewPost(postContent, numOfImages, selectedImages)
    }
}