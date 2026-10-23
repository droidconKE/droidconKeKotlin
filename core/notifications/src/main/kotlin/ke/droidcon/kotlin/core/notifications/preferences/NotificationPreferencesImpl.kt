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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPreferencesImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : NotificationPreferencesStore {
        override val preferences: Flow<NotificationPreferences> =
            dataStore.data
                .map { stored ->
                    val defaults = NotificationPreferences()
                    NotificationPreferences(
                        sessionRemindersEnabled = stored[SESSION_REMINDERS] ?: defaults.sessionRemindersEnabled,
                        scheduleChangesEnabled = stored[SCHEDULE_CHANGES] ?: defaults.scheduleChangesEnabled,
                        announcementsEnabled = stored[ANNOUNCEMENTS] ?: defaults.announcementsEnabled,
                        socialFeedEnabled = stored[SOCIAL_FEED] ?: defaults.socialFeedEnabled,
                        milestonesEnabled = stored[MILESTONES] ?: defaults.milestonesEnabled,
                        reminderLeadTimeMinutes = stored[LEAD_TIME] ?: defaults.reminderLeadTimeMinutes,
                        permissionPrompted = stored[PERMISSION_PROMPTED] ?: defaults.permissionPrompted,
                    )
                }.catch { error ->
                    Timber.e(error, "Error reading notification preferences")
                    emit(NotificationPreferences())
                }

        override suspend fun setChannelEnabled(
            channel: NotificationChannelSpec,
            enabled: Boolean,
        ) {
            val key = channelKey(channel) ?: return
            update { it[key] = enabled }
        }

        override suspend fun setReminderLeadTime(minutes: Int) {
            update { it[LEAD_TIME] = minutes }
        }

        override suspend fun markPermissionPrompted() {
            update { it[PERMISSION_PROMPTED] = true }
        }

        private suspend fun update(transform: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
            try {
                dataStore.edit(transform)
            } catch (e: Exception) {
                Timber.e(e, "Error updating notification preferences")
            }
        }

        private fun channelKey(channel: NotificationChannelSpec) =
            when (channel) {
                NotificationChannelSpec.SessionReminders -> SESSION_REMINDERS
                NotificationChannelSpec.ScheduleChanges -> SCHEDULE_CHANGES
                NotificationChannelSpec.Announcements -> ANNOUNCEMENTS
                NotificationChannelSpec.SocialFeed -> SOCIAL_FEED
                NotificationChannelSpec.Milestones -> MILESTONES
                NotificationChannelSpec.Sync -> null
            }

        private companion object {
            val SESSION_REMINDERS = booleanPreferencesKey("notifications_session_reminders")
            val SCHEDULE_CHANGES = booleanPreferencesKey("notifications_schedule_changes")
            val ANNOUNCEMENTS = booleanPreferencesKey("notifications_announcements")
            val SOCIAL_FEED = booleanPreferencesKey("notifications_social_feed")
            val MILESTONES = booleanPreferencesKey("notifications_milestones")
            val LEAD_TIME = intPreferencesKey("notifications_reminder_lead_time_minutes")
            val PERMISSION_PROMPTED = booleanPreferencesKey("notifications_permission_prompted")
        }
    }