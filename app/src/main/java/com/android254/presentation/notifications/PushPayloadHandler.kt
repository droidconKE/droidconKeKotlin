/*
 * Copyright 2026 DroidconKE
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

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.activity.MainActivity
import com.android254.presentation.common.navigation.DeepLinks
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.droidcon.kotlin.R
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import ke.droidcon.kotlin.core.notifications.push.PushPayload
import ke.droidcon.kotlin.core.notifications.push.PushPayload.ScheduleChange.ChangeType
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import ke.droidcon.kotlin.core.common.R as CommonR

/** Turns a decoded FCM [PushPayload] into what the user sees, or into a sync. */
interface PushPayloadHandler {
    suspend fun handle(payload: PushPayload)
}

/** The key in an FCM data message that carries the JSON-encoded [PushPayload]. */
const val PAYLOAD_KEY = "payload"

/** Decodes the payload in an FCM message's [data] and handles it; drops what it cannot read. */
suspend fun PushPayloadHandler.dispatch(data: Map<String, String>) {
    val json =
        data[PAYLOAD_KEY] ?: run {
            Timber.w("FCM message has no '$PAYLOAD_KEY' field; dropped")
            return
        }
    val payload =
        PushPayload.decodeOrNull(json) ?: run {
            Timber.w("FCM payload is malformed or of an unknown type; dropped")
            return
        }
    handle(payload)
}

/**
 * Posts each payload on its own channel, honouring the user's notification preferences.
 *
 * A sync request is not a notification, so it syncs whatever the preferences say.
 */
@Singleton
class PushPayloadHandlerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val preferencesStore: NotificationPreferencesStore,
        private val syncDataWorkManager: SyncDataWorkManager,
    ) : PushPayloadHandler {
        override suspend fun handle(payload: PushPayload) {
            when (payload) {
                is PushPayload.ScheduleChange ->
                    post(
                        channel = NotificationChannelSpec.ScheduleChanges,
                        key = "schedule_change:${payload.sessionId}",
                        title = context.getString(payload.changeType.titleRes()),
                        text = payload.message,
                        tapIntent = viewIntent(DeepLinks.session(payload.sessionId)),
                    )

                is PushPayload.Announcement ->
                    post(
                        channel = NotificationChannelSpec.Announcements,
                        key = "announcement:${payload.title}",
                        title = payload.title,
                        text = payload.body,
                        // A link the app cannot open would make the tap do nothing.
                        tapIntent =
                            payload.deepLink
                                ?.takeIf { DeepLinks.parse(it) != null }
                                ?.let(::viewIntent)
                                ?: launchIntent(),
                    )

                is PushPayload.NewFeedPost ->
                    post(
                        channel = NotificationChannelSpec.SocialFeed,
                        key = "feed_post:${payload.postId}",
                        title = context.getString(R.string.push_feed_post_title),
                        text = payload.preview,
                        tapIntent = viewIntent(DeepLinks.feed()),
                    )

                is PushPayload.Milestone ->
                    post(
                        channel = NotificationChannelSpec.Milestones,
                        key = "milestone:${payload.title}",
                        title = payload.title,
                        text = payload.body,
                        tapIntent = launchIntent(),
                    )

                PushPayload.SyncRequest -> syncDataWorkManager.startSync()
            }
        }

        @SuppressLint("MissingPermission")
        private suspend fun post(
            channel: NotificationChannelSpec,
            key: String,
            title: String,
            text: String,
            tapIntent: Intent,
        ) {
            if (!preferencesStore.preferences.first().isChannelEnabled(channel)) {
                Timber.d("Push on ${channel.id} dropped: channel disabled")
                return
            }
            if (!canPostNotifications()) {
                Timber.d("Push on ${channel.id} dropped: POST_NOTIFICATIONS not granted")
                return
            }

            // Keyed by type as well as id, so a schedule change never replaces the session's reminder.
            val id = key.hashCode()
            val contentIntent =
                PendingIntent.getActivity(
                    context,
                    id,
                    tapIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            val notification =
                NotificationCompat
                    .Builder(context, channel.id)
                    .setSmallIcon(CommonR.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .build()

            NotificationManagerCompat.from(context).notify(id, notification)
        }

        private fun canPostNotifications(): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

        private fun viewIntent(deepLink: String): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).setPackage(context.packageName)

        private fun launchIntent(): Intent = Intent(context, MainActivity::class.java)

        private fun ChangeType.titleRes(): Int =
            when (this) {
                ChangeType.RoomChanged -> R.string.push_room_changed
                ChangeType.TimeChanged -> R.string.push_time_changed
                ChangeType.Cancelled -> R.string.push_session_cancelled
                ChangeType.SpeakerChanged -> R.string.push_speaker_changed
            }
    }