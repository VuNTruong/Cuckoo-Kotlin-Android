package com.beta.myhbt_api.View.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.R

class RecyclerViewAdapterLocationPage (locationPageOptions: ArrayList<String>, locationPageOptionPic: ArrayList<Int>, locationPageOptionOnClickListener: ArrayList<View.OnClickListener>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Content of the option
    private val locationPageOptions = locationPageOptions

    // Icon of the option
    private val locationPageOptionPic = locationPageOptionPic

    // On click listeners of the options
    private val locationPageOptionOnClickListener = locationPageOptionOnClickListener

    // ViewHolder for the header
    inner class ViewHolderLocationPageHeader internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // The function to set up the header
        fun setUpHeader () {}
    }

    // ViewHolder for the option row
    inner class ViewHolderOptionRow internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val optionContent : TextView = itemView.findViewById(R.id.locationPageOption)
        private val optionIcon : ImageView = itemView.findViewById(R.id.locationPageOptionPic)
        private val mView = itemView

        // The function to set up the option row
        fun setUpOptionRow (optionContentParam: String, optionIconParam: Int, onClickListener: View.OnClickListener) {
            // Set content for the option
            optionContent.text = optionContentParam

            // Set icon for the option
            optionIcon.setImageResource(optionIconParam)

            // Set on click listener for the option
            mView.setOnClickListener(onClickListener)
        }
    }

    // ViewHolder for the footer
    inner class ViewHolderLocationPageFooter internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // The function to set up the footer row
        fun setUpFooter () {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view : View

        // Base on view type to return the right view holder
        // View type 0 is for the header
        return when (viewType) {
            0 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.location_page_header, parent, false)

                // Return the view holder
                ViewHolderLocationPageHeader(view)
            } // View type 1 is for the options
            1 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.location_page_options, parent, false)

                // Return the view holder
                ViewHolderOptionRow(view)
            } // View type 2 is for the footer
            else -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.location_page_footer, parent, false)

                // Return the view holder
                ViewHolderLocationPageFooter(view)
            }
        }
    }

    override fun getItemCount(): Int {
        // Return number of rows
        return 2 + locationPageOptions.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when  {
            position == 0 -> {
                // First row should be the header
                (holder as ViewHolderLocationPageHeader).setUpHeader()
            }
            (position >= 1 && position <= locationPageOptions.size) -> {
                // From row 2, show the options
                (holder as ViewHolderOptionRow).setUpOptionRow(
                    locationPageOptions[position - 1],
                    locationPageOptionPic[position - 1],
                    locationPageOptionOnClickListener[position - 1]
                )
            }
            else -> {
                // Last row will show the footer
                (holder as ViewHolderLocationPageFooter).setUpFooter()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when  {
            position == 0 -> {
                // First row should be the header
                0
            }
            (position >= 1 && position <= locationPageOptions.size) -> {
                // From row 2, show the options
                1
            }
            else -> {
                // Last row will show the footer
                2
            }
        }
    }
}