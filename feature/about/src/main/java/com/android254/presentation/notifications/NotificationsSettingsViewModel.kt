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

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.repos.SessionsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.notifications.NotificationPermission
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import ke.droidcon.kotlin.core.notifications.reminders.SessionReminderScheduler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ChannelUiState(
    val channel: NotificationChannelSpec,
    val enabled: Boolean,
    /** Switched off in system settings, which the in-app switch cannot override. */
    val blockedInSystem: Boolean,
)

@Immutable
data class NotificationsUiState(
    val channels: ImmutableList<ChannelUiState> = persistentListOf(),
    val reminderLeadTimeMinutes: Int = 10,
    /** The app may not post at all: permission denied, or switched off in system settings. */
    val notificationsBlocked: Boolean = false,
) {
    val sessionRemindersEnabled: Boolean
        get() = channels.any { it.channel == NotificationChannelSpec.SessionReminders && it.enabled && !it.blockedInSystem }
}

@HiltViewModel
class NotificationsSettingsViewModel
    @Inject
    constructor(
        private val preferencesStore: NotificationPreferencesStore,
        private val sessionsRepo: SessionsRepo,
        private val sessionReminderScheduler: SessionReminderScheduler,
        private val notificationPermission: NotificationPermission,
    ) : ViewModel() {
        private val systemState = MutableStateFlow(readSystemState())

        val uiState: StateFlow<NotificationsUiState> =
            combine(preferencesStore.preferences, systemState) { prefs, system ->
                NotificationsUiState(
                    channels =
                        NotificationChannelSpec.configurable
                            .map { channel ->
                                ChannelUiState(
                                    channel = channel,
                                    enabled = prefs.isChannelEnabled(channel),
                                    blockedInSystem = channel in system.blockedChannels,
                                )
                            }.toImmutableList(),
                    reminderLeadTimeMinutes = prefs.reminderLeadTimeMinutes,
                    notificationsBlocked = system.notificationsBlocked,
                )
            }.stateIn(viewModelScope, SharingStarted.Eagerly, NotificationsUiState())

        /** Re-reads what the system allows, e.g. on return from system settings. */
        fun refreshSystemState() {
            systemState.update { readSystemState() }
        }

        fun setChannelEnabled(
            channel: NotificationChannelSpec,
            enabled: Boolean,
        ) {
            viewModelScope.launch {
                preferencesStore.setChannelEnabled(channel, enabled)
                if (channel == NotificationChannelSpec.SessionReminders) rescheduleReminders()
            }
        }

        fun setReminderLeadTime(minutes: Int) {
            viewModelScope.launch {
                preferencesStore.setReminderLeadTime(minutes)
                rescheduleReminders()
            }
        }

        private suspend fun rescheduleReminders() {
            val starred = sessionsRepo.fetchSessions().first().filter { it.isBookmarked }
            sessionReminderScheduler.rescheduleAll(starred)
        }

        private fun readSystemState() =
            SystemState(
                notificationsBlocked = !notificationPermission.areNotificationsEnabled(),
                blockedChannels = NotificationChannelSpec.configurable.filter(notificationPermission::isChannelBlocked).toSet(),
            )

        private data class SystemState(
            val notificationsBlocked: Boolean,
            val blockedChannels: Set<NotificationChannelSpec>,
        )
    }