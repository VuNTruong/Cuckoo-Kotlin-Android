package com.beta.myhbt_api.ViewModel

import android.content.Context
import com.beta.myhbt_api.Model.UserCommentInteraction
import com.beta.myhbt_api.Model.UserInteraction
import com.beta.myhbt_api.Model.UserLikeInteraction
import com.beta.myhbt_api.Model.UserProfileVisit
import com.beta.myhbt_api.Repository.UserStatsRepositories.UserStatsRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UserStatsViewModel (context: Context) {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // User stats repository
    private val userStatsRepository: UserStatsRepository = UserStatsRepository(executorService, context)

    // The function to get account stats of the currently logged in user
    fun getCurrentUserAccountStats (callback: (arrayOfUserInteraction: ArrayList<UserInteraction>, arrayOfUserLikeInteraction: ArrayList<UserLikeInteraction>,
                                               arrayOfUserCommentInteraction: ArrayList<UserCommentInteraction>, arrayOfUserProfileVisit: ArrayList<UserProfileVisit>) -> Unit) {
        // Call the function to get account stats for current user
        userStatsRepository.getBriefUserStats { arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit ->
            // Return account stats info to view via callback function
            callback(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit)
        }
    }
}