package com.beta.cuckoo.View.VideoChat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.beta.cuckoo.R

class VideoChatIncomingCall : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat_incoming_call)

        // Hide the action bar
        supportActionBar!!.hide()
    }
}