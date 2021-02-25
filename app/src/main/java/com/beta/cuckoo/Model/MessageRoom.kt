package com.beta.cuckoo.Model

class MessageRoom (user1: String, user2: String, messageRoomId: String) {
    // Info of the message room
    private val user1 = user1
    private val user2 = user2
    private val messageRoomId = messageRoomId

    // Getters
    fun getUser1 () : String {
        return user1
    }

    fun getUser2 () : String {
        return user2
    }

    fun getMessageRoomId () : String {
        return messageRoomId
    }
}