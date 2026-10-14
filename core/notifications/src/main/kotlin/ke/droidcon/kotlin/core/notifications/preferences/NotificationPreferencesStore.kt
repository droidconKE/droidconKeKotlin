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
import kotlinx.coroutines.flow.Flow

/**
 * Persists and retrieves user notification preferences.
 *
 * Provides a reactive interface to read and modify notification settings,
 * including master control and per-channel toggles.
 */
interface NotificationPreferencesStore {
    /**
     * Emits the current notification preferences whenever they change.
     *
     * Errors during reading are caught and default preferences are emitted instead.
     */
    val preferences: Flow<NotificationPreferences>

    /**
     * Updates the master notification switch.
     *
     * @param enabled true to enable all notifications, false to disable all
     */
    suspend fun setMasterEnabled(enabled: Boolean)

    /**
     * Toggles a specific notification channel.
     *
     * @param channel The channel to update
     * @param enabled true to enable this channel, false to disable it
     */
    suspend fun setChannelEnabled(channel: NotificationChannelSpec, enabled: Boolean)

    /**
     * Updates the reminder lead time.
     *
     * @param minutes How many minutes before an event to send a reminder
     */
    suspend fun setReminderLeadTime(minutes: Int)
}
