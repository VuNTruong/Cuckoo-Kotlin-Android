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

    // The function to search for user based on full name
    fun searchUserBasedOnFullName (searchQuery: String, callback: (listOfUsers: ArrayList<User>) -> Unit) {
        // Call the function to get list of users based on full name
        userInfoRepository.searchUser(searchQuery) {arrayOfUsers ->
            // Return array of found users based on search query via callback function
            callback(arrayOfUsers)
        }
    }

    // The function to search for user around last updated location of the currently logged in user
    fun searchUserAround (searchQuery: String, callback: (listOfUsers: ArrayList<User>) -> Unit) {
        // Call the function to get list of users around the last updated location of the currently logged in user
        userInfoRepository.searchUserAround(searchQuery) {arrayOfUsers ->
            // Return array of found users based on search query via callback function
            callback(arrayOfUsers)
        }
    }
}