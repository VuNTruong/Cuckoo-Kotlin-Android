package com.beta.cuckoo.View.PostRecommend

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.PostPhoto
import com.beta.cuckoo.R
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterRecommendAlbum
import com.beta.cuckoo.ViewModel.PhotoViewModel
import kotlinx.android.synthetic.main.activity_post_recommend.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PostRecommend : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Photo view model
    private lateinit var photoViewModel: PhotoViewModel

    // Current location in list (will be mutated each time loaded)
    private var currentLocationInList = 0

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterRecommendAlbum?= null

    // Array of photos recommended for the user
    private var arrayOfRecommendedPhotos = ArrayList<PostPhoto>()

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_recommend)

        // Hide the navigation bar
        supportActionBar!!.hide()

        // Set up on click listener for the back button
        backButtonPostRecommend.setOnClickListener {
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Instantiate photo view model
        photoViewModel = PhotoViewModel(applicationContext)

        // Instantiate the recycler view
        recommendAlbumView.layoutManager = LinearLayoutManager(applicationContext)
        recommendAlbumView.itemAnimator = DefaultItemAnimator()

        // Call the function to get user id of the currently logged in user
        getPhotosForUser()
    }

    //*********************************** GET INFO OF CURRENT USER SEQUENCE ***********************************
    // The function to get list of recommended photos for the currently logged in user
    private fun getPhotosForUser () {
        // Call the function to get list of recommended photos for the currently logged in user
        photoViewModel.loadListOfRecommendedPhotos { arrayOfRecommendedPhotosParam, newCurrentLocationInList ->
            // Update the list of recommended photos
            arrayOfRecommendedPhotos.addAll(arrayOfRecommendedPhotosParam)

            // Update current location in list
            currentLocationInList = newCurrentLocationInList

            // Update the adapter
            adapter = RecyclerViewAdapterRecommendAlbum(arrayOfRecommendedPhotos, this, this, executorService)

            // Add adapter to the RecyclerView
            recommendAlbumView.adapter = adapter
        }
    }

    // The function to load more recommended photos for the currently logged in user
    fun getMorePhotosForUser () {
        // Call the function to get more recommended photos for the currently logged in user
        photoViewModel.loadMoreRecommendedPhotos (currentLocationInList) {arrayOfRecommendedPhotosParam, newCurrentLocationInList ->
            // Update the list of recommended photos
            arrayOfRecommendedPhotos.addAll(arrayOfRecommendedPhotosParam)

            // Update current location in list
            currentLocationInList = newCurrentLocationInList

            // Update the recycler view
            recommendAlbumView.adapter!!.notifyDataSetChanged()
        }
    }
}