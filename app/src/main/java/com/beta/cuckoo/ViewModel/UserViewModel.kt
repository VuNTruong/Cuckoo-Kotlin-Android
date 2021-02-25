package com.beta.cuckoo.ViewModel

import android.content.Context
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
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

    // The function to get array of user object of followers of user with specified user id
    fun getListOfFollowerUserObject (userId: String, callback: (arrayOfUserId: ArrayList<String>) -> Unit) {
        // Call the function to get list of user id of followers of user with specified user id
        userInfoRepository.getListOfFollowers(userId) {listOfUserId ->
            // Return array of user id via callback function
            callback(listOfUserId)
        }
    }

    // The function to get array of user object of followings of user wih specified user id
    fun getListOfFollowingUserObject (userId: String, callback: (arrayOfUserId: ArrayList<String>) -> Unit) {
        // Call the function to get list of user id of followings of user with specified user id
        userInfoRepository.getListOfFollowing(userId) {listOfUserId ->
            // Return array of user id via callback function
            callback(listOfUserId)
        }
    }
}