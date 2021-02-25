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

    // The function to get list of users who interact with currently logged in user
    fun getListOfUserInteraction (callback: (arrayOfUserInteraction: ArrayList<UserInteraction>) -> Unit) {
        // Call the function to get list user interaction
        userStatsRepository.getListOfUserInteraction { arrayOfUserInteraction ->
            // Return array of user interaction to view via callback function
            callback(arrayOfUserInteraction)
        }
    }

    // The function to get list of users who like post of currently logged in user
    fun getListOfUserLikeInteraction (callback: (arrayOfUserLikeInteraction: ArrayList<UserLikeInteraction>) -> Unit) {
        // Call the function to get list of user like interaction
        userStatsRepository.getListOfUserLikeInteraction{arrayOfUserLikeInteraction ->
            // Return array of user like interaction to view via callback function
            callback(arrayOfUserLikeInteraction)
        }
    }

    // The function to get list of users who comment on post of currently logged in user
    fun getListOfUserCommentInteraction (callback: (arrayOfUserCommentInteraction: ArrayList<UserCommentInteraction>) -> Unit) {
        // Call the function to get list of user comment interaction
        userStatsRepository.getListUserCommentInteraction { arrayOfUserCommentInteraction ->
            // Return array of user comment interaction to view via callback function
            callback(arrayOfUserCommentInteraction)
        }
    }

    // The function to get list of users who visit profile of the currently logged in user
    fun getListOfUserProfileVisit (callback: (arrayOfUserProfileVisit: ArrayList<UserProfileVisit>) -> Unit) {
        // Call the function to get list of user profile visit
        userStatsRepository.getListOfUserProfileVisit { arrayOfUserProfileVisit ->
            // Return array of user profile visit to view via callback function
            callback(arrayOfUserProfileVisit)
        }
    }
}