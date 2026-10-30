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

data class NotificationPreferences(
    val sessionRemindersEnabled: Boolean = true,
    val scheduleChangesEnabled: Boolean = true,
    val announcementsEnabled: Boolean = true,
    val socialFeedEnabled: Boolean = true,
    val milestonesEnabled: Boolean = true,
    val reminderLeadTimeMinutes: Int = ReminderLeadTime.Ten.minutes,
    /** Whether the app has already shown the system notification prompt from a star. */
    val permissionPrompted: Boolean = false,
) {
    /** The sync channel only carries the foreground notification of a running sync, so it is never off. */
    fun isChannelEnabled(channel: NotificationChannelSpec): Boolean =
        when (channel) {
            NotificationChannelSpec.SessionReminders -> sessionRemindersEnabled
            NotificationChannelSpec.ScheduleChanges -> scheduleChangesEnabled
            NotificationChannelSpec.Announcements -> announcementsEnabled
            NotificationChannelSpec.SocialFeed -> socialFeedEnabled
            NotificationChannelSpec.Milestones -> milestonesEnabled
            NotificationChannelSpec.Sync -> true
        }
}

enum class ReminderLeadTime(
    val minutes: Int,
) {
    Five(5),
    Ten(10),
    Fifteen(15),
    Thirty(30),
}