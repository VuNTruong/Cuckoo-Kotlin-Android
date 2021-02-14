package com.beta.myhbt_api.ViewModel

import android.content.Context
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UserViewModel (context: Context) {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Repository for the user info
    private val userInfoRepository: UserRepository =
        UserRepository(
            executorService,
            context
        )

    // The function to load info of the currently logged in user
    fun getCurrentUserInfo (callback: (userObject: User) -> Unit) {
        userInfoRepository.getInfoOfCurrentUser { userObject ->
            // Define what to be returned in the callback function
            callback(userObject)
        }
    }
}