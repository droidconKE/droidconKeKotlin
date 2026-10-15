/*
 * Copyright 2025 DroidconKE
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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.repos.SessionsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val masterEnabled: Boolean = true,
    val channelStates: Map<NotificationChannelSpec, Boolean> = emptyMap(),
    val reminderLeadTimeMinutes: Int = 10,
    val isLoading: Boolean = false,
)

@HiltViewModel
class NotificationsSettingsViewModel @Inject constructor(
    private val preferencesStore: NotificationPreferencesStore,
    private val sessionsRepo: SessionsRepo,
    private val sessionReminderScheduler: SessionReminderScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesStore.preferences.collect { prefs ->
                val channelStates = NotificationChannelSpec.entries.associateWith { channel ->
                    prefs.isChannelEnabled(channel)
                }

                _uiState.update {
                    it.copy(
                        masterEnabled = prefs.masterEnabled,
                        reminderLeadTimeMinutes = prefs.reminderLeadTimeMinutes,
                        channelStates = channelStates,
                    )
                }
            }
        }
    }

    fun setMasterEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesStore.setMasterEnabled(enabled)
            _uiState.update { it.copy(masterEnabled = enabled) }

            if (enabled) {
                rescheduleReminders()
            }
        }
    }

    fun setChannelEnabled(channel: NotificationChannelSpec, enabled: Boolean) {
        viewModelScope.launch {
            preferencesStore.setChannelEnabled(channel, enabled)
            _uiState.update { state ->
                state.copy(
                    channelStates = state.channelStates.toMutableMap().apply {
                        this[channel] = enabled
                    },
                )
            }
        }
    }

    fun setReminderLeadTime(minutes: Int) {
        viewModelScope.launch {
            preferencesStore.setReminderLeadTime(minutes)
            _uiState.update { it.copy(reminderLeadTimeMinutes = minutes) }

            rescheduleReminders()
        }
    }

    private suspend fun rescheduleReminders() {
        if (!_uiState.value.masterEnabled) return

        val sessions = sessionsRepo.fetchSessions().first()
        val starred = sessions.filter { it.isBookmarked }
        sessionReminderScheduler.rescheduleAll(starred)
    }
}
