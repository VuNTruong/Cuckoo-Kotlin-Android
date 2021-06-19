package com.beta.cuckoo.Repository.PostRepositories

import android.content.Context
import android.widget.Toast
import com.beta.cuckoo.Network.*
import com.beta.cuckoo.Network.LikesAndComments.*
import com.beta.cuckoo.Network.Posts.*
import com.beta.cuckoo.Model.CuckooPost
import com.beta.cuckoo.Model.PostComment
import com.beta.cuckoo.Model.PostPhoto
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor
import java.util.stream.Stream

class PostRepository (executor: Executor, context: Context) {
    // The executor to do work in background thread
    private val executor = executor

    // Context of the parent activity
    private val context = context

    // The user repository (to get info of the currently logged in user for some uses)
    private val userRepository = UserRepository(executor, context)

    // The function to get posts for the currently logged in user based on id
    fun getPostsForUser (userId: String, currentLocationInList: Int, callback: (postArray: ArrayList<CuckooPost>, newCurrentLocationInList: Int, status: String) -> Unit) {
        executor.execute{
            // Create the get all posts service
            val getAllPostService: GetAllPostService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetAllPostService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getAllPostService.getAllPosts(userId, currentLocationInList)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body (array of posts)
                        val hbtGramPostsArray = (responseBody["data"]) as ArrayList<CuckooPost>

                        // Get new order in collection to load next series of posts
                        val newCurrentLocationInList = ((responseBody["newCurrentLocationInList"]) as Double).toInt()

                        // Callback function
                        callback(hbtGramPostsArray, newCurrentLocationInList, "Done")
                    } else {
                        // Callback function
                        callback(ArrayList(), 0, "Error occur")
                    }
                }
            })
        }
    }

    // The function to get info of the latest post
    fun getInfoOfLatestPost (callback: (latestPostOrderInCollection: Int) -> Unit) {
        executor.execute{
            // Create the get latest post service
            val getLatestPostService: GetInfoOfLatestPostService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetInfoOfLatestPostService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getLatestPostService.getLatestPostInfo()

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body (order in collection of the latest post)
                        // We also add 1 to it so that latest post maybe included as well (if it is for the user)
                        val latestPostOrderInCollection = (responseBody["data"] as Double).toInt()

                        // Define what to return in the callback function
                        callback(latestPostOrderInCollection)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to get first photo of the post with specified post id
    fun getFirstPhotoOfPost (postId: String, callback: (firstPhotoURL: String) -> Unit) {
        executor.execute{
            // Create the get first image URL service
            val getFirstImageURLService: GetFirstImageURLOfPostService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetFirstImageURLOfPostService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getFirstImageURLService.getFirstPhotoURL(postId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get the array of images
                        val arrayOfImages = data["documents"] as List<Map<String, Any>>

                        if (arrayOfImages.isNotEmpty()) {
                            // Get image info from the received data
                            val firstImageInfo = (data["documents"] as List<Map<String, Any>>)[0]

                            // Get URL of the image
                            val firstImageURL = firstImageInfo["imageURL"] as String

                            // Return URL of first image of the post
                            callback(firstImageURL)
                        }
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to load number of comments for the post with the specified post id
    fun getNumberOfCommentsForPost (postId: String, callback: (numOfComments: Int) -> Unit) {
        executor.execute{
            // Create the get post comments service
            val getPostCommentsService: GetPostCommentsService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetPostCommentsService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getPostCommentsService.getPostComments(postId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        val body = response.body()
                        print(body)
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get number of comments
                        val numOfComments = (responseBody["results"] as Double).toInt()

                        // Return num of comments via callback function
                        callback(numOfComments)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to load number of likes for the post with the specified post id
    fun getNumberOfLikesForPost (postId: String, callback: (numOfLikes: Int) -> Unit) {
        executor.execute{
            // Create the get post likes service
            val getPostLikesService: GetAllPostLikesService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetAllPostLikesService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getPostLikesService.getPostLikes(postId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        val body = response.body()
                        print(body)
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get number of likes
                        val numOfLikes = (responseBody["results"] as Double).toInt()

                        // Return number of likes
                        callback(numOfLikes)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to create new like for the post (liked by the currently logged in user)
    fun createLikeForPost (postId: String, callback: (likerId: String) -> Unit) {
        executor.execute{
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the add like service
                val addLikeService: CreateNewPostLikeService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                    CreateNewPostLikeService::class.java)

                // The call object which will then be used to perform the API call
                val call: Call<Any> = addLikeService.createNewHBTGramPostLike(userObject.getId(), postId)

                // Perform the API call
                call.enqueue(object: Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        // Report the error if something is not right
                        print("Boom")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // Notify the activity that like has been created via callback function
                        callback(userObject.getId())
                    }
                })
            }
        }
    }

    // The function to create new comment for the post (commented by the currently logged in user)
    fun createCommentForPost (commentContent: String, postId: String, callback: (commentWriterId: String) -> Unit) {
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
                            // Notify the activity that comment has been created via callback function
                            callback(userObject.getId())
                        }
                    }
                })
            }
        }
    }

    // The function to check like status between a post and the currently logged in user
    fun checkLikeStatusOfPostAndCurrentUser (postId: String, callback: (liked: Boolean) -> Unit) {
        executor.execute {
            // Call the function to get info of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Create the check like status service
                val checkHBTGramListStatusService: CheckPostLikeStatusService =
                    RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                        CheckPostLikeStatusService::class.java
                    )

                // The call object which will then be used to perform the API call
                val call: Call<Any> =
                    checkHBTGramListStatusService.checkHBTGramPostLikeStatus(userObject.getId(), postId)

                // Perform the API call
                call.enqueue(object : Callback<Any> {
                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        // Report the error if something is not right
                        print("Boom")
                    }

                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        // If the response body is null, it means that comment can't be posted
                        if (response.body() == null) {
                            // Show the error
                            Toast.makeText(context, "Something is not right", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            // Body of the request
                            val responseBody = response.body() as Map<String, Any>

                            // Get like status from body of the response
                            val likeStatus = responseBody["status"] as String

                            // If like status is "Done. User has liked post", the user has liked the post and set like button to be the red heart
                            // Otherwise, let it be the blank heart
                            if (likeStatus == "Done. User has liked post") {
                                // Let the activity know that user has liked the post via callback function
                                callback(true)
                            } else {
                                // Let the activity know that user has not liked the post via callback function
                                callback(false)
                            }
                        }
                    }
                })
            }
        }
    }

    // The function to get post detail of the post with the specified post id (return array of comments and array of images)
    fun getPostDetail (postId: String, callback: (arrayOfImages: ArrayList<PostPhoto>, arrayOfComments: ArrayList<PostComment>, status: String) -> Unit) {
        executor.execute{
            // Create the get post detail service
            val getHBTGramPostDetail: GetPostDetail = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetPostDetail::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getHBTGramPostDetail.getPostDetail(postId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get array of images of the post
                        val arrayOfImages = responseBody["arrayOfImages"] as ArrayList<PostPhoto>

                        // Get array of comments
                        val arrayOfComments = responseBody["arrayOfComments"] as ArrayList<PostComment>

                        // Return array of images and comments of the post to the view model via callback function
                        callback(arrayOfImages, arrayOfComments, "Done")
                    } else {
                        // Return error to the view model
                        callback(ArrayList(), ArrayList(), "Error")
                    }
                }
            })
        }
    }

    // The function to get photo of the post comment based on comment id (in case comment has image)
    fun getPhotoOfComment (commentId: String, callback: (commentPhotoURL: String) -> Unit) {
        executor.execute {
            // Create the get photo of comment service
            val getHBTGramPostCommentPhotoService: GetPostCommentPhotoService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetPostCommentPhotoService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getHBTGramPostCommentPhotoService.getPostCommentPhoto(commentId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response body
                        val data = responseBody["data"] as Map<String, Any>

                        // Get comment photo info from the received data
                        val photoInfo = (data["documents"] as List<Map<String, Any>>)[0]

                        // Get image URL of comment photo
                        val commentPhotoImageURL = photoInfo["imageURL"] as String

                        // Return comment photo to the view model via callback function
                        callback(commentPhotoImageURL)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to get posts nearby location of the currently logged in user
    fun getPostAroundOfCurrentUser (callback: (arrayOfPosts: ArrayList<CuckooPost>, locationForNextLoad: Int) -> Unit) {
        executor.execute {
            // Call the function to get last updated location of the currently logged in user
            userRepository.getLocationOfCurrentUser { lastUpdatedLocation ->
                // Call the function to get location in list of the latest post in collection
                getInfoOfLatestPost { latestPostOrderInCollection ->
                    // Call the function to load posts within a last updated location of the currently logged in user
                    // Create the get posts around service
                    val getPostsWithinARadiusService: GetPostsWithinARadiusService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                        GetPostsWithinARadiusService::class.java)

                    // Create the call object in order to perform the call
                    val call: Call<Any> = getPostsWithinARadiusService.getPostsWithinARadius("${lastUpdatedLocation.latitude},${lastUpdatedLocation.longitude}", 50, latestPostOrderInCollection)

                    // Perform the call
                    call.enqueue(object: Callback<Any> {
                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            print("Boom")
                        }

                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            // If the response body is not empty it means that the token is valid
                            if (response.body() != null) {
                                // Body of the request
                                val responseBody = response.body() as Map<String, Any>

                                // Get data from the response body (array of posts)
                                val hbtGramPostsArray = responseBody["data"] as ArrayList<CuckooPost>

                                // Get new order in collection to load next series of posts
                                val newCurrentLocationInList = (responseBody["newCurrentLocationInList"] as Double).toInt()

                                // Update new current location in list (location in list for next load)
                                // If order in collection to load next series of post is null, let it be 0
                                callback(hbtGramPostsArray, newCurrentLocationInList)
                            } else {
                                print("Something is not right")
                            }
                        }
                    })
                }
            }
        }
    }

    // The function to get post object based on post id
    fun getPostObjectBasedOnId (postId: String, callback: (postObject: CuckooPost, foundPost: Boolean) -> Unit) {
        executor.execute {
            // If the post detail is empty, get out of the sequence
            if (postId == "") {
                // Let view model know that there is no post found via callback function
                callback(CuckooPost("", "","",0,0,""), false)
            }

            // Create the get post based on id service
            val getPostBasedOnIdService: GetPostBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetPostBasedOnIdService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getPostBasedOnIdService.getPostBasedOnId(postId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("There seem be be an error ${t.stackTrace}")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data from the response
                        val data = (((responseBody["data"] as Map<String, Any>)["documents"]) as ArrayList<Map<String, Any>>)[0]

                        // In order to prevent us from encountering the class cast exception, we need to do the following
                        // Create the GSON object
                        val gs = Gson()

                        // Convert a linked tree map into a JSON string
                        val jsPost = gs.toJson(data)

                        // Convert the JSOn string back into HBTGramPost class
                        val hbtGramPostModel = gs.fromJson<CuckooPost>(jsPost, CuckooPost::class.java)

                        // Return post object via callback function
                        callback(hbtGramPostModel, true)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to get list of likes of the post with specified post id
    fun getListOfLikes (postId: String, callback: (listOfUsers: ArrayList<String>) -> Unit) {
        executor.execute {
            // Create the get post likes service
            val getPostLikesService: GetAllPostLikesService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetAllPostLikesService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getPostLikesService.getPostLikes(postId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Array of user id of users who like the post
                        val arrayOfLiker = ArrayList<String>()

                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get data of the response
                        val data = responseBody["data"] as Map<String, Any>

                        // Get data from the response
                        val listOfLikes = data["documents"] as ArrayList<Map<String, Any>>

                        // Loop through that list of likes, get liker info based on their id
                        for (like in listOfLikes) {
                            // Add user id of liker to the array of users who like the post
                            arrayOfLiker.add(like["whoLike"] as String)
                        }

                        // Return array of user ids of user who like post with the specified post id via callback function
                        callback(arrayOfLiker)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to get number of posts created by user with specified user id
    fun getNumOfPostsCreatedByUserWithId (userId: String, callback: (numOfPosts: Int) -> Unit) {
        executor.execute {
            // Create the service for getting number of posts
            val getPostsOfUserService: GetPostsOfUserService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                GetPostsOfUserService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = getPostsOfUserService.getPostsOfUser(userId)

            // Perform the call
            call.enqueue(object : Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("There seem to be an error ${t.stackTrace}")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that there is data
                    if (response.body() != null) {
                        // Body of the request
                        val responseBody = response.body() as Map<String, Any>

                        // Get number of posts
                        val numOfPosts = (responseBody["results"] as Double).toInt()

                        // Return number of posts via callback function
                        callback(numOfPosts)
                    }
                }
            })
        }
    }

    // The function to delete a post with specified post id
    fun deletePost (postId: String, callback: (postDeleted: Boolean) -> Unit) {
        executor.execute {
            // Create the delete post service
            val deletePostService: DeletePostService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                DeletePostService::class.java)

            // Create the call object in order to perform the call
            val call: Call<Any> = deletePostService.deletePost(postId)

            // Perform the call
            call.enqueue(object: Callback<Any> {
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Boom")
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // If the response body is not empty it means that the token is valid
                    if (response.body() != null) {
                        // Let the view know that post has been deleted via callback function
                        callback(true)
                    } else {
                        print("Something is not right")
                    }
                }
            })
        }
    }

    // The function to update post with specified post id
    fun updatePost (postId: String, postContent: String, callback: (isUpdated: Boolean) -> Unit) {
        executor.execute {
            // Create the update post service
            val updatePostService: UpdatePostService = RetrofitClientInstance.getRetrofitInstance(context)!!.create(
                UpdatePostService::class.java
            )

            // Create the call object to perform the call
            val call: Call<Any> = updatePostService.updatePost(postContent, postId)

            // Perform the call
            call.enqueue(object : Callback<Any> {
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    // Let the view know that post has been updated
                    callback(true)
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    print("Something is not right")
                }
            })
        }
    }
}