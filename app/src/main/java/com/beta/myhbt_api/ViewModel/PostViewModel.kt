package com.beta.myhbt_api.ViewModel

import android.content.Context
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.HBTGramPostComment
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.Repository.PostRepositories.PostRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PostViewModel (context: Context) {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Repository for the post
    private val postRepository: PostRepository =
        PostRepository(
            executorService,
            context
        )

    // The function to load posts for the user
    fun loadPosts (userId: String, callback: (postArray: ArrayList<HBTGramPost>, newCurrentLocationInList: Int) -> Unit) {
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
    fun loadMorePosts (userId: String, currentLocationInList: Int, callback: (postArray: ArrayList<HBTGramPost>, newCurrentLocationInList: Int) -> Unit) {
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
    fun getPostDetail (postId: String, callback: (arrayOfImages: ArrayList<HBTGramPostPhoto>, arrayOfComments: ArrayList<HBTGramPostComment>) -> Unit) {
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
}