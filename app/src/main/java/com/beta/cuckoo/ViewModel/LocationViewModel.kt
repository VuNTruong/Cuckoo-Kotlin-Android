package com.beta.cuckoo.ViewModel

import android.content.Context
import com.beta.cuckoo.Repository.LocationRepositories.LocationRepository
import com.beta.cuckoo.Repository.UserRepositories.FollowRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LocationViewModel (context: Context) {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Location repository
    private val locationRepository = LocationRepository(executorService, context)

    // Follow repository
    private val followRepository = FollowRepository(executorService, context)
}