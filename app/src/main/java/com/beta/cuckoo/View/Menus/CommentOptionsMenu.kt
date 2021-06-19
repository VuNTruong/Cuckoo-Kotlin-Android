package com.beta.cuckoo.View.Menus

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.beta.cuckoo.Model.PostComment
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.PostRepositories.PostCommentRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.concurrent.Executor

class CommentOptionsMenu (parentActivity: Activity, commentId: String, executor: Executor) : BottomSheetDialogFragment() {
    // The parent activity
    private val parentActivity = parentActivity

    // Comment id of post to be modified
    private val commentId = commentId

    // Executor to do work in the background
    private val executor = executor

    // Comment repository
    private lateinit var commentRepository: PostCommentRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        // The view object
        val view = inflater.inflate(R.layout.comment_option_menu_item, container, false)

        // The delete comment button
        val deleteCommentButton: CardView = view.findViewById(R.id.deleteCommentButton)

        // Instantiate the comment repository
        commentRepository = PostCommentRepository(executor, parentActivity)

        // Set up on click listener for the delete comment button
        deleteCommentButton.setOnClickListener {
            // Dismiss the menu
            this.dismiss()

            // Call the function to start deleting comment
            deleteComment(commentId)
        }

        // Return the view
        return view
    }

    // The function to start deleting a comment
    private fun deleteComment (commentId: String) {
        // Show the waiting indicator
        val progress = ProgressDialog(parentActivity)
        progress.setTitle("Processing...")
        progress.setMessage("Hang on while we are deleting comment...")
        progress.setCancelable(false) // disable dismiss by tapping outside of the dialog

        // Show the progress bar
        progress.show()

        // Call the function to delete a comment
        commentRepository.deleteComment(commentId) {isDeleted ->
            if (isDeleted) {
                // Dismiss the waiting dialog
                progress.dismiss()
            }
        }
    }
}