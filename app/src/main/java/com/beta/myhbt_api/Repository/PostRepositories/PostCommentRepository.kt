package com.beta.myhbt_api.Repository.PostRepositories

import android.content.Context
import android.widget.Toast
import com.beta.myhbt_api.Network.LikesAndComments.CreateNewPostCommentPhotoService
import com.beta.myhbt_api.Network.LikesAndComments.CreateNewPostCommentService
import com.beta.myhbt_api.Network.RetrofitClientInstance
import com.beta.myhbt_api.Repository.UserRepositories.UserRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class PostCommentRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The user repository (to get info of the currently logged in user for some uses)
    private val userRepository = UserRepository(executor, context)

    // The function to create new comment photo
    fun createNewCommentPhoto (commentId: String, imageURL: String, callback: (uploadDone: Boolean) -> Unit) {
        executor.execute {
            // Create the create comment photo service
            val createCommentPhotoService: CreateNewPostCommentPhotoService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                CreateNewPostCommentPhotoService::class.java)

            // The call object which will then be used to perform the API call
            val call: Call<Any> = createCommentPhotoService.createNewHBTGramPostCommentPhoto(commentId, imageURL)

            // Perform the API call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    // Report the error if something is not right
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // Let the view know that comment photo has been uploaded
                    callback(true)
                }
            })
        }
    }

    // The function to create new comment for the post (commented by the currently logged in user)
    fun createCommentForPost (commentContent: String, postId: String, callback: (commentCreated: Boolean, newCommentId: String) -> Unit) {
        executor.execute{
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the create comment service
                val postCommentService: CreateNewPostCommentService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    CreateNewPostCommentService::class.java)

                // The call object which will then be used to perform the API call
                val call: Call<Any> = postCommentService.createNewHBTGramPostComment(commentContent, userObject.getId(), postId)

                // Perform the API call
                call.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        // Report the error if something is not right
                        print("Boom")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // If the response body is null, it means that comment can't be posted
                        if (response.body() == null) {
                            // Show the alert
                            Toast.makeText(context, "Comment can't be posted", Toast.LENGTH_SHORT).show()
                        } else {
                            // Body of the request
                            val responseBody = response.body() as Map<String, Any>

                            // Get data of the response from response body
                            val data = (responseBody["data"] as Map<String, Any>)["tour"] as Map<String, Any>

                            // Get id of the newly created comment
                            val newCommentId = data["_id"] as String

                            // Notify the activity that comment has been created via callback function
                            callback(true, newCommentId)
                        }
                    }
                })
            }
        }
    }
}