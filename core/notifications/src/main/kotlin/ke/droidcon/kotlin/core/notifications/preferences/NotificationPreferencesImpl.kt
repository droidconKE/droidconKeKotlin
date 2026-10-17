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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed implementation of NotificationPreferencesStore.
 *
 * Persists notification preferences to a JSON file on disk and provides
 * reactive updates via Flow.
 */
@Singleton
class NotificationPreferencesImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : NotificationPreferencesStore {
        private val dataStore: DataStore<NotificationPreferences> =
            DataStoreFactory.create(
                serializer = NotificationPreferencesSerializer,
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = {
                    context.filesDir.resolve("notification_preferences.json")
                },
                corruptionHandler = ReplaceFileCorruptionHandler { NotificationPreferences() },
            )

        override val preferences: Flow<NotificationPreferences> =
            dataStore.data.catch { error ->
                Timber.e(error, "Error reading notification preferences")
                emit(NotificationPreferences())
            }

        override suspend fun setMasterEnabled(enabled: Boolean) {
            updatePreferences { it.copy(masterEnabled = enabled) }
        }

        override suspend fun setChannelEnabled(
            channel: NotificationChannelSpec,
            enabled: Boolean,
        ) {
            updatePreferences { prefs ->
                when (channel) {
                    NotificationChannelSpec.SessionReminders ->
                        prefs.copy(sessionRemindersEnabled = enabled)

                    NotificationChannelSpec.ScheduleChanges ->
                        prefs.copy(scheduleChangesEnabled = enabled)

                    NotificationChannelSpec.Announcements ->
                        prefs.copy(announcementsEnabled = enabled)

                    NotificationChannelSpec.SocialFeed ->
                        prefs.copy(socialFeedEnabled = enabled)

                    NotificationChannelSpec.Milestones ->
                        prefs.copy(milestonesEnabled = enabled)

                    NotificationChannelSpec.Sync -> prefs.copy(syncEnabled = enabled)
                }
            }
        }

        override suspend fun setReminderLeadTime(minutes: Int) {
            updatePreferences { it.copy(reminderLeadTimeMinutes = minutes) }
        }

        override suspend fun markPermissionPrompted() {
            updatePreferences { it.copy(permissionPrompted = true) }
        }

        private suspend fun updatePreferences(
            transform: suspend (NotificationPreferences) -> NotificationPreferences,
        ) {
            try {
                dataStore.updateData { transform(it) }
            } catch (e: Exception) {
                Timber.e(e, "Error updating notification preferences")
            }
        }
    }