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
import androidx.compose.ui.tooling.preview.Preview
import com.droidconke.chai.ChaiTheme
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
private fun NotificationsSettingsPhonePreview() {
    ChaiTheme {
        NotificationsSettingsContent(
            uiState =
                NotificationsUiState(
                    masterEnabled = true,
                    channelStates = NotificationChannelSpec.entries.associateWith { true },
                    reminderLeadTimeMinutes = 10,
                ),
            onMasterEnabledChange = {},
            onChannelEnabledChange = { _, _ -> },
            onReminderLeadTimeChange = {},
            onOpenSystemSettings = {},
            onNavigateBack = {},
        )
    }
}

@Preview(device = "spec:width=1280dp,height=800dp")
@Composable
private fun NotificationsSettingsTabletPreview() {
    ChaiTheme {
        NotificationsSettingsContent(
            uiState =
                NotificationsUiState(
                    masterEnabled = true,
                    channelStates = NotificationChannelSpec.entries.associateWith { true },
                    reminderLeadTimeMinutes = 10,
                ),
            onMasterEnabledChange = {},
            onChannelEnabledChange = { _, _ -> },
            onReminderLeadTimeChange = {},
            onOpenSystemSettings = {},
            onNavigateBack = {},
        )
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
private fun NotificationsSettingsBlockedPreview() {
    ChaiTheme {
        NotificationsSettingsContent(
            uiState =
                NotificationsUiState(
                    masterEnabled = true,
                    channelStates = NotificationChannelSpec.entries.associateWith { true },
                    reminderLeadTimeMinutes = 10,
                    notificationsBlocked = true,
                ),
            onMasterEnabledChange = {},
            onChannelEnabledChange = { _, _ -> },
            onReminderLeadTimeChange = {},
            onOpenSystemSettings = {},
            onNavigateBack = {},
        )
    }
}