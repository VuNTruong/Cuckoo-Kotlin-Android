package com.beta.myhbt_api.View.Menus

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.beta.myhbt_api.Controller.Posts.DeletePostService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostOptionsMenu (parentActivity: Activity, postId: String) : BottomSheetDialogFragment() {
    // The parent activity
    private val parentActivity = parentActivity

    // Post id of the post to be edited or deleted
    private val postId = postId

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        // The view object
        val view = inflater.inflate(R.layout.post_option_menu_item, container, false)

        // The delete post button
        val deletePostButton: CardView = view.findViewById(R.id.deletePostButton)

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

        // Create the delete post service
        val deletePostService: DeletePostService = RetrofitClientInstance.getRetrofitInstance(parentActivity)!!.create(
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
                    // Dismiss the waiting dialog
                    progress.dismiss()

                    // build alert dialog
                    val dialogBuilder = AlertDialog.Builder(parentActivity)

                    // set message of alert dialog
                    dialogBuilder.setMessage("Post has been deleted")
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
                    print("Something is not right")
                }
            }
        })
    }
}