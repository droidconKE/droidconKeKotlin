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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.data.notifications.NotificationChannelSpec
import com.droidconke.chai.ChaiTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ke.droidcon.kotlin.core.screenshot.Preview

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
fun NotificationsSettingsScreen_Phone_Light() {
    ChaiTheme(isDark = false) {
        NotificationsSettingsScreen(
            viewModel = FakeNotificationsSettingsViewModel(),
            onNavigateBack = {},
        )
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
fun NotificationsSettingsScreen_Phone_Dark() {
    ChaiTheme(isDark = true) {
        NotificationsSettingsScreen(
            viewModel = FakeNotificationsSettingsViewModel(),
            onNavigateBack = {},
        )
    }
}

@Preview(device = "spec:width=1280dp,height=800dp")
@Composable
fun NotificationsSettingsScreen_Tablet() {
    ChaiTheme(isDark = false) {
        NotificationsSettingsScreen(
            viewModel = FakeNotificationsSettingsViewModel(),
            onNavigateBack = {},
        )
    }
}

@Preview(device = "spec:width=1920dp,height=1080dp")
@Composable
fun NotificationsSettingsScreen_Desktop() {
    ChaiTheme(isDark = false) {
        NotificationsSettingsScreen(
            viewModel = FakeNotificationsSettingsViewModel(),
            onNavigateBack = {},
        )
    }
}

private class FakeNotificationsSettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        NotificationsUiState(
            masterEnabled = true,
            channelStates = NotificationChannelSpec.entries.associateWith { true },
            reminderLeadTimeMinutes = 10,
            isLoading = false,
        ),
    )
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun setMasterEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(masterEnabled = enabled)
    }

    fun setChannelEnabled(channel: NotificationChannelSpec, enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            channelStates = _uiState.value.channelStates.toMutableMap().apply {
                this[channel] = enabled
            },
        )
    }

    fun setReminderLeadTime(minutes: Int) {
        _uiState.value = _uiState.value.copy(reminderLeadTimeMinutes = minutes)
    }
}
