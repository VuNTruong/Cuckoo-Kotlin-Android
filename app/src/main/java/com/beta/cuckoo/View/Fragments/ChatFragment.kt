package com.beta.cuckoo.View.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.MessageRoom
import com.beta.cuckoo.R
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterMessageRoom
import com.beta.cuckoo.View.Chat.SearchUserToChatWith
import com.beta.cuckoo.ViewModel.MessageViewModel
import kotlinx.android.synthetic.main.fragment_chat.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ChatFragment : Fragment() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Message repository
    private lateinit var messageRepository: MessageViewModel

    // Array of message room
    private var arrayOfMessageRooms = ArrayList<MessageRoom>()

    // Adapter for the RecyclerView
    private lateinit var adapter : RecyclerViewAdapterMessageRoom

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate the notification repository
        messageRepository = MessageViewModel(this.requireContext())

        // Instantiate the recycler view
        messageRoomView.layoutManager = LinearLayoutManager(this@ChatFragment.requireActivity())
        messageRoomView.itemAnimator = DefaultItemAnimator()

        // Set up on click listener for the create new message button
        createNewMessageButton.setOnClickListener {
            // Take user to the activity where the user can search for user and send message to that one
            val intent = Intent(this@ChatFragment.requireActivity(), SearchUserToChatWith::class.java)
            startActivity(intent)
        }

        // Call the function to get info of the currently logged in user which will then call the function
        // to get message rooms in which the current user is in
        getMessageRoomsOfCurrentUser()
    }

    // The function to get list of message rooms of the currently logged in user
    private fun getMessageRoomsOfCurrentUser () {
        // Call the function to get list of message rooms for the currently logged in user\
        messageRepository.getMessageRoomsOfUser { messageRooms ->
            // Update the array of message rooms
            arrayOfMessageRooms = messageRooms

            // Update the adapter
            adapter = RecyclerViewAdapterMessageRoom(arrayOfMessageRooms, this@ChatFragment.requireActivity(), executorService)

            // Add adapter to the RecyclerView
            messageRoomView.adapter = adapter
        }
    }
}