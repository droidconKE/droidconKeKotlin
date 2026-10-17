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

/**
 * Specification for notification channels used by the droidcon Kenya app.
 *
 * Each channel defines a separate category of notifications with distinct importance levels
 * and user preferences. Channels are grouped under the "droidcon Kenya" channel group.
 */
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
        private const val CHANNEL_GROUP_ID = "conference"

        /**
         * Creates all notification channels and their group in the NotificationManager.
         *
         * This should be called during app initialization to set up the notification infrastructure.
         * It creates a channel group and adds all six notification channels to it.
         *
         * @param context The Android application context
         */
        fun createAll(context: Context) {
            val notificationManager =
                context.getSystemService<NotificationManager>()
                    ?: return

            // Create the channel group
            val channelGroup = NotificationChannelGroup(CHANNEL_GROUP_ID, context.getString(R.string.channel_group_conference))
            notificationManager.createNotificationChannelGroup(channelGroup)

            // Create all notification channels
            entries.forEach { spec ->
                val channel = NotificationChannel(spec.id, getChannelName(context, spec), spec.importance)
                channel.description = getChannelDescription(context, spec)
                channel.group = CHANNEL_GROUP_ID
                notificationManager.createNotificationChannel(channel)
            }
        }

        private fun getChannelName(
            context: Context,
            spec: NotificationChannelSpec,
        ): CharSequence =
            if (spec.nameResId != 0) {
                context.getString(spec.nameResId)
            } else {
                // Fallback to the enum name until string resources are available (Task 1.5)
                spec.name
            }

        private fun getChannelDescription(
            context: Context,
            spec: NotificationChannelSpec,
        ): String =
            if (spec.descriptionResId != 0) {
                context.getString(spec.descriptionResId)
            } else {
                // Fallback to empty string until string resources are available (Task 1.5)
                ""
            }
    }
}