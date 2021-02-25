package com.beta.cuckoo.ViewModel

import android.content.Context
import com.beta.cuckoo.Model.PostPhoto
import com.beta.cuckoo.Repository.PostRepositories.PhotoRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PhotoViewModel (context: Context) {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Repository for the photo
    private val photoRepository: PhotoRepository = PhotoRepository(executorService, context)

    // The function to get list of recommended photos for the first load for the currently logged in user
    fun loadListOfRecommendedPhotos (callback: (arrayOfRecommendedPhotos: ArrayList<PostPhoto>, newCurrentLocationInList: Int) -> Unit) {
        // Call the function to get order in collection of latest photo in collection
        photoRepository.getOrderInCollectionOfLatestPhoto { orderInCollectionOfLatestPhoto ->
            // Call the function to get list of photos
            photoRepository.getRecommendedPhotos(orderInCollectionOfLatestPhoto) {arrayOfRecommendedPhotos, newCurrentLocationInList ->
                // Return array of recommended photos and current location in list via callback function
                callback(arrayOfRecommendedPhotos, newCurrentLocationInList)
            }
        }
    }

    // The function to load more recommended photos based on current location in list of the user
    fun loadMoreRecommendedPhotos (currentLocationInList: Int, callback: (arrayOfRecommendedPhotos: ArrayList<PostPhoto>, newCurrentLocationInList: Int) -> Unit) {
        // Call the function to get list of photos
        photoRepository.getRecommendedPhotos(currentLocationInList) {arrayOfRecommendedPhotos, newCurrentLocationInList ->
            // Return array of recommended photos and current location in list via callback function
            callback(arrayOfRecommendedPhotos, newCurrentLocationInList)
        }
    }

    // The function to load photos created by user with specified user id
    fun loadPhotosCreatedByUserWithSpecifiedId (userId: String, callback: (arrayOfImagesByUser: ArrayList<PostPhoto>) -> Unit) {
        // Call the function to get list of photos created by user with specified user id
        photoRepository.getPhotosOfUserWithId(userId) {arrayOfImagesByUser ->
            // Return array of photos created by user with specified id via callback function
            callback(arrayOfImagesByUser)
        }
    }
}