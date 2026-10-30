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

import androidx.compose.runtime.Composable
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import org.junit.Test

class NotificationsSettingsScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun notifications() = captureScreen("screens/notifications") { Settings() }

    @Test
    fun `notifications blocked`() = captureScreen("screens/notifications_blocked") { Settings(blocked = true) }

    @Test
    fun `notifications across form factors`() = captureFormFactors("form_factors/notifications") { Settings() }
}

@Composable
private fun Settings(blocked: Boolean = false) {
    NotificationsSettingsContent(
        uiState = sampleNotificationsUiState().copy(notificationsBlocked = blocked),
        onChannelEnabledChange = { _, _ -> },
        onReminderLeadTimeChange = {},
        onOpenSystemSettings = {},
        onNavigateBack = {},
    )
}