package com.beta.cuckoo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.beta.cuckoo.View.AudioChat.AudioChatIncomingCall
import com.beta.cuckoo.View.Chat.SearchUserToChatWith
import com.beta.cuckoo.View.Locations.UpdateLocation
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.beta.cuckoo.View.VideoChat.VideoChatIncomingCall
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson

class FirebaseMessagingService () : FirebaseMessagingService() {
    // gson converter
    private val gson = Gson()

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        val messageDataTitle = remoteMessage.data["title"]
        val messageDataContent = remoteMessage.data["body"]

        // If title of the notification is "video-chat-received", start the call ("data" for now)
        if (messageDataTitle == "data") {
            // Go to the incoming call activity
            val intent = Intent(applicationContext, VideoChatIncomingCall::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Pass chat room id to the incoming call activity (it will be in body of the notification)
            intent.putExtra("chatRoomId", (messageDataContent!!.split("-").toTypedArray())[0])

            // Pass caller user id to the incoming call activity
            intent.putExtra("callerUserId", (messageDataContent.split("-").toTypedArray())[1])

            // Start the incoming call activity
            startActivity(intent)
        }

        // If title of the notification is "audio-chat-received", start the audio call
        if (messageDataTitle == "audio-chat-received") {
            // Go to the incoming audio call activity
            val intent = Intent(applicationContext, AudioChatIncomingCall::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Pass chat room id to the incoming audio call activity
            intent.putExtra("chatRoomId", (messageDataContent!!.split("-").toTypedArray()[0]))

            // Pass caller user id to the incoming audio call activity
            intent.putExtra("callerUserId", (messageDataContent!!.split("-").toTypedArray()[1]))

            // Start the incoming audio call activity
            startActivity(intent)
        }

        val messageData = remoteMessage.data["data"]
        if (messageData == "data") {
            // Go to the incoming call activity
            val intent = Intent(applicationContext, UpdateLocation::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Start the activity
            startActivity(intent)
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            // Get title and body of the notification
            val title = it.title
            val body = it.body

            // If title of the notification is "video-chat-received", start the call ("title" for now)
            when (title) {
                "title" -> {
                    // Go to the incoming call activity
                    val intent = Intent(applicationContext, VideoChatIncomingCall::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    // Pass chat room id to the incoming call activity (it will be in body of the notification)
                    intent.putExtra("chatRoomId", (body!!.split("-").toTypedArray())[0])

                    // Pass caller user id to the incoming call activity
                    intent.putExtra("callerUserId", (body.split("-").toTypedArray())[1])

                    // Start the incoming call activity
                    startActivity(intent)
                }
                "cancelledCall" -> {
                    val intent = Intent("finish")
                    sendBroadcast(intent)
                }
                else -> {
                    // Also if you intend on generating your own notifications as a result of a received FCM
                    // message, here is where that should be initiated. See sendNotification method below.
                    sendNotification("Cloud message received")
                }
            }

            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")

        // Get currently saved FCM token of the user
        val currentToken = MainMenu.preferences.getString("FCMToken", "")

        // Get user id of the currently logged in user (saved in memory)
        val currentUserId = MainMenu.preferences.getString("currentUserId", "")

        // Save received token into memory
        MainMenu.memory.putString("FCMToken", token).apply()
        MainMenu.memory.commit()

        // Bring user into the notification room
        MainMenu.mSocket.emit(
            "jumpInNotificationRoom", gson.toJson(
                hashMapOf(
                    "userId" to currentUserId,
                    "socketId" to token,
                    "oldSocketId" to currentToken
                )
            )
        )
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: String) {
        /*
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)

         */

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.cuckoologo1)
            .setContentTitle("Notification")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            //.setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "notification_channel",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}