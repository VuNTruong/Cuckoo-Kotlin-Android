package com.beta.myhbt_api.View.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Model.PostPhoto
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterRecommendAlbum
import com.beta.myhbt_api.ViewModel.PhotoViewModel
import kotlinx.android.synthetic.main.fragment_recommend_photo.recommendAlbumView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RecommendAlbumFragment: Fragment() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Photo view model
    private lateinit var photoViewModel: PhotoViewModel

    // Current location in list (will be mutated each time loaded)
    private var currentLocationInList = 0

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterRecommendAlbum ?= null

    // Array of photos recommended for the user
    private var arrayOfRecommendedPhotos = ArrayList<PostPhoto>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_recommend_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate photo view model
        photoViewModel = PhotoViewModel(this.requireContext())

        // Instantiate the recycler view
        recommendAlbumView.layoutManager = LinearLayoutManager(this@RecommendAlbumFragment.context)
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
            adapter = RecyclerViewAdapterRecommendAlbum(arrayOfRecommendedPhotos, this@RecommendAlbumFragment.requireActivity(), this@RecommendAlbumFragment, executorService)

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