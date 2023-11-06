/*
 * Copyright 2023 DroidconKE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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