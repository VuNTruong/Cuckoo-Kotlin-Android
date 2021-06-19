package com.beta.cuckoo.View.Menus

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.PostRepositories.PostRepository
import com.beta.cuckoo.View.Posts.UpdatePost
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.post_option_menu_item.*
import java.util.concurrent.Executor

class PostOptionsMenu (parentActivity: Activity, postId: String, executor: Executor) : BottomSheetDialogFragment() {
    // The parent activity
    private val parentActivity = parentActivity

    // Post id of the post to be edited or deleted
    private val postId = postId

    // Executor to do work in the background
    private val executor = executor

    // Post repository
    private lateinit var postRepository: PostRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        // The view object
        val view = inflater.inflate(R.layout.post_option_menu_item, container, false)

        // Instantiate post repository
        postRepository = PostRepository(executor, parentActivity)

        // The delete post button
        val deletePostButton: CardView = view.findViewById(R.id.deletePostButton)

        // The edit post button
        val editPostButton: CardView = view.findViewById(R.id.editPostButton)

        // Handle on click listener for the delete button
        deletePostButton.setOnClickListener {
            // Dismiss the menu
            this.dismiss()

            // Ask the user to make sure that user really wants to delete post
            // build alert dialog
            val dialogBuilder = AlertDialog.Builder(parentActivity)

            // set message of alert dialog
            dialogBuilder.setMessage("Are you sure that you really want to delete this post?")
                // User say yes
                .setPositiveButton("Yes") { _, _ ->
                    // Call the function to start deleting post
                    deletePost()
                }
                // User say no
                .setNegativeButton("Hang on!") { _, _ -> }

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("Delete post?")
            // show alert dialog
            alert.show()
        }

        // Handle on click listener for the edit button
        editPostButton.setOnClickListener {
            // Dismiss the menu
            this.dismiss()

            // Take user to the activity where user can update post
            val intent = Intent(parentActivity, UpdatePost::class.java)

            // Let the activity know which post to update
            intent.putExtra("postId", postId)

            // Start the activity
            parentActivity.startActivity(intent)
        }

        // Return the view
        return view
    }

    // The function to start deleting a post
    private fun deletePost () {
        // Show the waiting indicator
        val progress = ProgressDialog(parentActivity)
        progress.setTitle("Processing...")
        progress.setMessage("Hang on while we are deleting your post...")
        progress.setCancelable(false) // disable dismiss by tapping outside of the dialog

        // Show the progress bar
        progress.show()

        // Call the function to delete a post
        postRepository.deletePost(postId) {postDeleted ->
            if (postDeleted) {
                // Dismiss the waiting dialog
                progress.dismiss()

                // build alert dialog
                val dialogBuilder = AlertDialog.Builder(parentActivity)

                // set message of alert dialog
                dialogBuilder.setMessage("Conversation has been deleted")
                    // if the dialog is cancelable
                    .setCancelable(false)
                    // positive button text and action
                    .setPositiveButton("OK") { _, _ ->
                        // Finish the parent activity
                        parentActivity.finish()
                    }

                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("Success!")
                // show alert dialog
                alert.show()
            } else {
                progress.dismiss()
            }
        }
    }
}