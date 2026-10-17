/*
 * Copyright 2023 DroidconKE
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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.droidconke.chai.ChaiTheme
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationsSettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var openedSystemSettings = 0

    private fun setContent(state: NotificationsUiState) {
        composeTestRule.setContent {
            ChaiTheme {
                NotificationsSettingsContent(
                    uiState = state,
                    onMasterEnabledChange = {},
                    onChannelEnabledChange = { _, _ -> },
                    onReminderLeadTimeChange = {},
                    onOpenSystemSettings = { openedSystemSettings++ },
                    onNavigateBack = {},
                )
            }
        }
    }

    private val allOn = NotificationChannelSpec.entries.associateWith { true }

    @Test
    fun `the blocked banner offers a way to system settings`() {
        setContent(NotificationsUiState(channelStates = allOn, notificationsBlocked = true))

        composeTestRule.onNodeWithText("Notifications are off for droidcon Kenya").assertIsDisplayed()
        composeTestRule.onNodeWithText("Turn on in Settings").performClick()

        assertEquals(1, openedSystemSettings)
    }

    @Test
    fun `no banner when notifications are allowed`() {
        setContent(NotificationsUiState(channelStates = allOn, notificationsBlocked = false))

        composeTestRule.onNodeWithText("Notifications are off for droidcon Kenya").assertDoesNotExist()
    }

    @Test
    fun `channel toggles are hidden while the master switch is off`() {
        setContent(NotificationsUiState(masterEnabled = false, channelStates = allOn))

        composeTestRule.onNodeWithText("Schedule Changes").assertDoesNotExist()
    }
}