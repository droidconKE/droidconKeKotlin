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
package ke.droidcon.kotlin.core.notifications

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService

enum class NotificationChannelSpec(
    val id: String,
    val nameResId: Int,
    val descriptionResId: Int,
    val importance: Int,
) {
    SessionReminders(
        id = "session_reminders",
        nameResId = R.string.channel_session_reminders,
        descriptionResId = R.string.channel_session_reminders_desc,
        importance = NotificationManager.IMPORTANCE_HIGH,
    ),
    ScheduleChanges(
        id = "schedule_changes",
        nameResId = R.string.channel_schedule_changes,
        descriptionResId = R.string.channel_schedule_changes_desc,
        importance = NotificationManager.IMPORTANCE_HIGH,
    ),
    Announcements(
        id = "announcements",
        nameResId = R.string.channel_announcements,
        descriptionResId = R.string.channel_announcements_desc,
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    ),
    SocialFeed(
        id = "social_feed",
        nameResId = R.string.channel_social_feed,
        descriptionResId = R.string.channel_social_feed_desc,
        importance = NotificationManager.IMPORTANCE_LOW,
    ),
    Milestones(
        id = "milestones",
        nameResId = R.string.channel_milestones,
        descriptionResId = R.string.channel_milestones_desc,
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    ),
    Sync(
        id = "sync",
        nameResId = R.string.channel_sync,
        descriptionResId = R.string.channel_sync_desc,
        importance = NotificationManager.IMPORTANCE_MIN,
    ),
    ;

    companion object {
        /** The channels people can switch off; sync only backs a running sync's own notification. */
        val configurable: List<NotificationChannelSpec> = entries.filter { it != Sync }

        private const val CHANNEL_GROUP_ID = "conference"

        /** Channels earlier versions created; left behind, they still show in system settings. */
        private val LEGACY_CHANNEL_IDS = listOf("DROIDCON_CHANNEL_ID", "notification_channel")

        fun createAll(context: Context) {
            val notificationManager =
                context.getSystemService<NotificationManager>()
                    ?: return

            LEGACY_CHANNEL_IDS.forEach(notificationManager::deleteNotificationChannel)

            val channelGroup = NotificationChannelGroup(CHANNEL_GROUP_ID, context.getString(R.string.channel_group_conference))
            notificationManager.createNotificationChannelGroup(channelGroup)

            entries.forEach { spec ->
                val channel = NotificationChannel(spec.id, context.getString(spec.nameResId), spec.importance)
                channel.description = context.getString(spec.descriptionResId)
                channel.group = CHANNEL_GROUP_ID
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}