package com.beta.cuckoo.View.Menus

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.beta.cuckoo.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CommentOptionsMenu (parentActivity: Activity, commentId: String) : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        // The view object
        val view = inflater.inflate(R.layout.comment_option_menu_item, container, false)

        // The delete comment button
        val deleteCommentButton: CardView = view.findViewById(R.id.deleteCommentButton)

        // Set up on click listener for the delete comment button
        deleteCommentButton.setOnClickListener {

        }

        // Return the view
        return view
    }
}