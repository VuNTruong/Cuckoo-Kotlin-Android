package com.beta.cuckoo.ViewModel

import android.content.Context
import com.beta.cuckoo.Model.Message
import com.beta.cuckoo.Model.MessageRoom
import com.beta.cuckoo.Repository.MessageRepositories.MessageRepository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MessageViewModel (context: Context) {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // Repository for the message
    private val messageRepository: MessageRepository = MessageRepository(executorService, context)

    // The function to get message rooms in which current user is in
    fun getMessageRoomsOfUser (callback: (messageRooms: ArrayList<MessageRoom>) -> Unit) {
        // Call the function to get list of message rooms for the currently logged in user
        messageRepository.getListOfMessageRoomsOfCurrentUser { messageRooms ->
            // Return array of message rooms via callback function
            callback(messageRooms)
        }
    }

    // The function to get messages in the specified message room
    fun getMessagesInMessageRoom (messageRoomId: String, callback: (messages: ArrayList<Message>) -> Unit) {
        // Call the function to get list of messages in a specified room id
        messageRepository.getMessagesOfMessageRoom(messageRoomId) {messages ->
            // Return array of messages via callback function
            callback(messages)
        }
    }

    // The function to send message from current user to the specified message receiver
    fun sendMessage (messageRoomId: String, messageReceiverUserId: String, messageContent: String, callback: (messageSentFirstTime: Boolean, messageObject: Message, chatRoomId: String, currentUserId: String) -> Unit) {
        // Call the function to send message
        messageRepository.sendMessage(messageRoomId, messageReceiverUserId, messageContent) {messageSentFirstTime, messageObject, chatRoomId, currentUserId: String ->
            // Return message sent first time status and newly sent message object via callback function
            callback(messageSentFirstTime, messageObject, chatRoomId, currentUserId)
        }
    }
}