package com.beta.cuckoo.View.Posts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.PostPhoto
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.PostRepositories.PostRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterCuckooPostDetail
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterPostPhotoUpdatePost
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.activity_update_post.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UpdatePost : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Post id of post to be edited
    private lateinit var postIdToBeEdited: String

    // Adapter for the RecyclerView which will be used to show photos of post
    private var adapter: RecyclerViewAdapterPostPhotoUpdatePost?= null

    // Post repository
    private lateinit var postRepository: PostRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_post)

        // Hide the action bar
        supportActionBar!!.hide()

        // Get post id of post to be edited from previous activity
        postIdToBeEdited = intent.getStringExtra("postId") as String

        // Instantiate post repository
        postRepository = PostRepository(executorService, applicationContext)

        // Instantiate recycler view
        photoOfPostToShow.layoutManager = LinearLayoutManager(applicationContext)
        photoOfPostToShow.itemAnimator = DefaultItemAnimator()

        // Set up on click listener for the update post button
        updatePostButton.setOnClickListener {
            // Call the function to update post with a new content
            updatePost(postIdToBeEdited, postContentToUpdate.text.toString())
        }

        // Call the function to get post detail of post which is going to be edited
        getPostDetail(postIdToBeEdited)
    }

    // The function get post object as well as photos that goes with post
    private fun getPostDetail (postId: String) {
        // Call the function to get post object of post with specified post id
        postRepository.getPostObjectBasedOnId(postId) {postObject, foundPost ->
            if (foundPost) {
                // Call the function to get list of photos that go with post with specified post id
                postRepository.getPostDetail(postId) {arrayOfImages, _, status ->
                    if (status == "Done") {
                        // Load post content into the edit text
                        postContentToUpdate.setText(postObject.getContent())

                        // Update the adapter
                        adapter = RecyclerViewAdapterPostPhotoUpdatePost(arrayOfImages, this)

                        // Update recycler view
                        photoOfPostToShow.adapter = adapter
                    }
                }
            }
        }
    }

    // The function to update post with new content
    private fun updatePost (postId: String, newPostContent: String) {
        // Call the function to update post
        postRepository.updatePost(postId, newPostContent) {isUpdated ->
            // If post is updated, show toast and let the user know that
            if (isUpdated) {
                Toast.makeText(applicationContext, "Updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Please try again", Toast.LENGTH_SHORT).show()
            }
        }
    }
}