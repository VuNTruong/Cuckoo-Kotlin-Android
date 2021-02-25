package com.beta.myhbt_api.View.Fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.R
import com.beta.myhbt_api.Repository.PostRepositories.CreatePostRepository
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterHBTGramPostPhoto
import kotlinx.android.synthetic.main.fragment_create_post.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CreatePostFragment : Fragment() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Post repository
    private lateinit var createPostRepository: CreatePostRepository

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterHBTGramPostPhoto?= null

    // Array of selected images for the post
    private val selectedImages = ArrayList<Uri>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the post repository
        createPostRepository = CreatePostRepository(executorService, this.requireContext())

        // Initialize the RecyclerView
        photoOfPostToCreate.layoutManager = LinearLayoutManager(this@CreatePostFragment.requireActivity())
        photoOfPostToCreate.itemAnimator = DefaultItemAnimator()

        // Update the adapter
        adapter = RecyclerViewAdapterHBTGramPostPhoto(selectedImages, this@CreatePostFragment.requireActivity(), this@CreatePostFragment)

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

    // The function to get extension of the image
    private fun getExtension(uri: Uri): String? {
        val contentResolver = this@CreatePostFragment.requireContext().contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
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