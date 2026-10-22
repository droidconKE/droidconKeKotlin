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

import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.droidconke.chai.ChaiTheme
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "w411dp-h2400dp")
class NotificationsSettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var openedSystemSettings = 0

    private var lastLeadTime: Int? = null
    private var lastToggle: Pair<NotificationChannelSpec, Boolean>? = null

    private fun setContent(state: NotificationsUiState) {
        composeTestRule.setContent {
            ChaiTheme {
                NotificationsSettingsContent(
                    uiState = state,
                    onChannelEnabledChange = { channel, enabled -> lastToggle = channel to enabled },
                    onReminderLeadTimeChange = { lastLeadTime = it },
                    onOpenSystemSettings = { openedSystemSettings++ },
                    onNavigateBack = {},
                )
            }
        }
    }

    private val allOn = sampleNotificationsUiState()

    @Test
    fun `the blocked banner offers a way to system settings`() {
        setContent(allOn.copy(notificationsBlocked = true))

        composeTestRule.onNodeWithText("Notifications are off for droidconKenya").assertIsDisplayed()
        composeTestRule.onNodeWithText("You won't get reminders for sessions you star.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Turn on").performClick()

        assertEquals(1, openedSystemSettings)
    }

    @Test
    fun `no banner when notifications are allowed`() {
        setContent(allOn)

        composeTestRule.onNodeWithText("Notifications are off for droidconKenya").assertDoesNotExist()
    }

    @Test
    fun `the system settings row is always there`() {
        setContent(allOn)

        composeTestRule.onNodeWithText("System notification settings").performClick()

        assertEquals(1, openedSystemSettings)
    }

    @Test
    fun `tapping a row's label toggles it`() {
        setContent(allOn)

        composeTestRule.onNodeWithText("Announcements").performClick()

        assertEquals(NotificationChannelSpec.Announcements to false, lastToggle)
    }

    @Test
    fun `every switch is labelled by its row`() {
        setContent(allOn)

        composeTestRule
            .onAllNodes(isToggleable())
            .assertCountEquals(NotificationChannelSpec.configurable.size)
        composeTestRule.onNode(isToggleable() and hasText("Announcements", substring = true)).assertExists()
    }

    @Test
    fun `switches are disabled while notifications are blocked`() {
        setContent(allOn.copy(notificationsBlocked = true))

        composeTestRule.onAllNodes(isToggleable()).assertAll(isNotEnabled())
    }

    @Test
    fun `a channel switched off in system settings says so and cannot be toggled`() {
        val blocked =
            allOn.copy(
                channels =
                    allOn.channels
                        .map { if (it.channel == NotificationChannelSpec.Announcements) it.copy(blockedInSystem = true) else it }
                        .toImmutableList(),
            )
        setContent(blocked)

        composeTestRule.onNodeWithText("Off in system settings").assertIsDisplayed()
        composeTestRule.onNode(isToggleable() and hasText("Announcements", substring = true)).assertIsNotEnabled()
    }

    @Test
    fun `the lead time choice shows while session reminders are on`() {
        setContent(allOn)

        composeTestRule.onNodeWithText("30 min").performClick()

        assertEquals(30, lastLeadTime)
    }

    @Test
    fun `the lead time choice is hidden while session reminders are off`() {
        setContent(
            allOn.copy(
                channels =
                    allOn.channels
                        .map { if (it.channel == NotificationChannelSpec.SessionReminders) it.copy(enabled = false) else it }
                        .toImmutableList(),
            ),
        )

        composeTestRule.onNodeWithText("30 min").assertDoesNotExist()
    }
}
