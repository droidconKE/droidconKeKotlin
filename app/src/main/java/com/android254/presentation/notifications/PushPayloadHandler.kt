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

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.activity.MainActivity
import com.android254.presentation.common.navigation.DeepLinks
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.droidcon.kotlin.R
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.notifications.NotificationPermission
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
    val json = data[PAYLOAD_KEY]
    val payload =
        if (json != null) {
            PushPayload.decodeOrNull(json) ?: run {
                Timber.w("FCM payload is malformed or of an unknown type; dropped")
                return
            }
        } else {
            legacyAnnouncement(data) ?: run {
                Timber.w("FCM message has neither '$PAYLOAD_KEY' nor '$LEGACY_MESSAGE_KEY'; dropped")
                return
            }
        }
    handle(payload)
}

private const val LEGACY_TITLE_KEY = "title"
private const val LEGACY_MESSAGE_KEY = "message"

// The format before typed payloads, kept so pushes still arrive while the backend moves over.
private fun legacyAnnouncement(data: Map<String, String>): PushPayload.Announcement? =
    data[LEGACY_MESSAGE_KEY]?.let { message ->
        PushPayload.Announcement(title = data[LEGACY_TITLE_KEY].orEmpty(), body = message)
    }

/**
 * Posts each payload on its own channel, honouring the user's notification preferences.
 *
 * A sync request is not a notification, so it syncs whatever the preferences say. A schedule
 * change syncs the same way, so the reminders of a moved session move too.
 */
@Singleton
class PushPayloadHandlerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val preferencesStore: NotificationPreferencesStore,
        private val syncDataWorkManager: SyncDataWorkManager,
        private val notificationPermission: NotificationPermission,
    ) : PushPayloadHandler {
        override suspend fun handle(payload: PushPayload) {
            when (payload) {
                is PushPayload.ScheduleChange -> {
                    // Whether or not the user sees it, reminders must move with the session.
                    syncDataWorkManager.startFreshSync()
                    post(
                        channel = NotificationChannelSpec.ScheduleChanges,
                        key = "schedule_change:${payload.sessionId}",
                        title = context.getString(payload.changeType.titleRes()),
                        text = payload.message,
                        tapIntent = viewIntent(DeepLinks.session(payload.sessionId)),
                    )
                }

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

                PushPayload.SyncRequest -> syncDataWorkManager.startFreshSync()
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
            if (notificationPermission.needsRuntimeRequest()) {
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