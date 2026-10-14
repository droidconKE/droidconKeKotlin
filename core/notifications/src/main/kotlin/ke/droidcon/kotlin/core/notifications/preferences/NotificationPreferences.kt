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
package ke.droidcon.kotlin.core.notifications.preferences

import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import kotlinx.serialization.Serializable

/**
 * User's notification preferences with master control and per-channel toggles.
 *
 * The master [masterEnabled] switch acts as an override for all notifications.
 * Individual channels are controlled by their respective toggles, but only take effect
 * if the master switch is enabled.
 */
@Serializable
data class NotificationPreferences(
    val masterEnabled: Boolean = true,
    val sessionRemindersEnabled: Boolean = true,
    val scheduleChangesEnabled: Boolean = true,
    val announcementsEnabled: Boolean = true,
    val socialFeedEnabled: Boolean = true,
    val milestonesEnabled: Boolean = true,
    val syncEnabled: Boolean = false,
    val reminderLeadTimeMinutes: Int = 10,
) {
    /**
     * Checks if a notification channel is enabled.
     *
     * A channel is enabled only if the master switch is on AND the channel's individual toggle is on.
     *
     * @param channel The notification channel to check
     * @return true if the channel should receive notifications, false otherwise
     */
    fun isChannelEnabled(channel: NotificationChannelSpec): Boolean {
        return masterEnabled && when (channel) {
            NotificationChannelSpec.SessionReminders -> sessionRemindersEnabled
            NotificationChannelSpec.ScheduleChanges -> scheduleChangesEnabled
            NotificationChannelSpec.Announcements -> announcementsEnabled
            NotificationChannelSpec.SocialFeed -> socialFeedEnabled
            NotificationChannelSpec.Milestones -> milestonesEnabled
            NotificationChannelSpec.Sync -> syncEnabled
        }
    }
}

/**
 * Enumeration of available reminder lead times.
 *
 * Specifies how many minutes before a session start a reminder should be sent.
 */
enum class ReminderLeadTime(val minutes: Int) {
    Five(5),
    Ten(10),
    Fifteen(15),
    Thirty(30),
}
