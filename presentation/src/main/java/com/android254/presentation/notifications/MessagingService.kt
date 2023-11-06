package com.android254.presentation.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: DroidconNotificationManager

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        notificationManager.createNotificationChannel("All")

        message.data["message"]?.let { notificationMessage ->
            notificationManager.showNotification(message = notificationMessage, title = message.data["title"].toString())
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

}