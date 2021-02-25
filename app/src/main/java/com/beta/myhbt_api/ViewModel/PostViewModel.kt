package com.beta.myhbt_api.ViewModel

import android.content.Context
import com.beta.myhbt_api.Model.CuckooPost
import com.beta.myhbt_api.Model.PostComment
import com.beta.myhbt_api.Model.PostPhoto
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.Repository.PostRepositories.PostRepository
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PostViewModel (context: Context) {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Repository for the post
    private val postRepository: PostRepository = PostRepository(executorService, context)

    // User repository
    private val userRepository: UserRepository = UserRepository(executorService, context)

    // The function to load posts for the user
    fun loadPosts (userId: String, callback: (postArray: ArrayList<CuckooPost>, newCurrentLocationInList: Int) -> Unit) {
        // Call the function in the repository to get info of the latest post in collection
        postRepository.getInfoOfLatestPost { latestPostOrderInCollection ->
            // Call the function in the repository to load posts
            postRepository.getPostsForUser(userId, latestPostOrderInCollection) {hbtGramPostsArray, newCurrentLocationInList, status ->
                if (status == "Done") {
                    // Define what to return in the callback function
                    callback(hbtGramPostsArray, newCurrentLocationInList)
                } else {
                    print("There seem to be an error")
                }
            }
        }
    }

    // The function to load more posts for the user based on new current location in list of the user
    fun loadMorePosts (userId: String, currentLocationInList: Int, callback: (postArray: ArrayList<CuckooPost>, newCurrentLocationInList: Int) -> Unit) {
        // Call the function in the repository to load posts
        postRepository.getPostsForUser(userId, currentLocationInList) {hbtGramPostsArray, newCurrentLocationInList, status ->
            if (status == "Done") {
                // Define what to return in the callback function
                callback(hbtGramPostsArray, newCurrentLocationInList)
            } else {
                print("There seem to be an error")
            }
        }
    }

    // The function to get post detail of the post with the specified post id
    fun getPostDetail (postId: String, callback: (arrayOfImages: ArrayList<PostPhoto>, arrayOfComments: ArrayList<PostComment>) -> Unit) {
        // Call the function in the repository to load post detail
        postRepository.getPostDetail(postId) {arrayOfImages, arrayOfComments, status ->
            if (status == "Done") {
                // Return array of images and comments to the activity
                callback(arrayOfImages, arrayOfComments)
            } else {
                print("There seem to be an error")
            }
        }
    }

    // The function to get list of user objects of user who like post with the specified post id
    fun getListOfUserWhoLikePost (postId: String, callback: (arrayOfUsersWhoLike: ArrayList<String>) -> Unit) {
        // Call the function to get list of user id of users who like post with specified id
        postRepository.getListOfLikes(postId) {listOfUsers ->
            // Return array of user ids of user who like post via callback function
            callback(listOfUsers)
        }
    }
}