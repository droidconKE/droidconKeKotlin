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

import com.android254.domain.models.Session
import com.android254.domain.repos.SessionsRepo
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.notifications.NotificationPermission
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferences
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import ke.droidcon.kotlin.core.notifications.reminders.SessionReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsSettingsViewModelTest {
    private val preferences = MutableStateFlow(NotificationPreferences())
    private val preferencesStore =
        mockk<NotificationPreferencesStore>(relaxed = true) {
            every { preferences } returns this@NotificationsSettingsViewModelTest.preferences
        }
    private val starred = session(id = "starred", isBookmarked = true)
    private val sessionsRepo =
        mockk<SessionsRepo> {
            every { fetchSessions() } returns flowOf(listOf(starred, session(id = "other", isBookmarked = false)))
        }
    private val scheduler = mockk<SessionReminderScheduler>(relaxed = true)
    private val notificationPermission = mockk<NotificationPermission>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { notificationPermission.areNotificationsEnabled() } returns true
        every { notificationPermission.isChannelBlocked(any()) } returns false
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = NotificationsSettingsViewModel(preferencesStore, sessionsRepo, scheduler, notificationPermission)

    @Test
    fun `notifications switched off in system settings show the banner on any API`() {
        every { notificationPermission.areNotificationsEnabled() } returns false

        assertTrue(viewModel().uiState.value.notificationsBlocked)
    }

    @Test
    fun `refreshing after the user allows notifications hides the banner`() {
        every { notificationPermission.areNotificationsEnabled() } returns false
        val viewModel = viewModel()

        every { notificationPermission.areNotificationsEnabled() } returns true
        viewModel.refreshSystemState()

        assertFalse(viewModel.uiState.value.notificationsBlocked)
    }

    @Test
    fun `changing the lead time reschedules starred sessions`() {
        viewModel().setReminderLeadTime(30)

        coVerify(exactly = 1) { scheduler.rescheduleAll(listOf(starred)) }
    }

    @Test
    fun `switching session reminders off reschedules so the scheduler cancels the reminders`() {
        viewModel().setChannelEnabled(NotificationChannelSpec.SessionReminders, false)

        coVerify(exactly = 1) { scheduler.rescheduleAll(listOf(starred)) }
    }

    @Test
    fun `only the channels people can switch are listed`() {
        val channels =
            viewModel()
                .uiState.value.channels
                .map { it.channel }

        assertEquals(NotificationChannelSpec.configurable, channels)
        assertFalse(NotificationChannelSpec.Sync in channels)
    }

    @Test
    fun `a channel switched off in system settings is reported as blocked`() {
        every { notificationPermission.isChannelBlocked(NotificationChannelSpec.Announcements) } returns true

        val announcements =
            viewModel()
                .uiState.value.channels
                .first { it.channel == NotificationChannelSpec.Announcements }

        assertTrue(announcements.blockedInSystem)
    }

    @Test
    fun `a switched off reminder channel hides the lead time choice`() {
        preferences.value = NotificationPreferences(sessionRemindersEnabled = false)

        assertFalse(viewModel().uiState.value.sessionRemindersEnabled)
    }

    @Test
    fun `switching another channel off leaves the reminders alone`() {
        viewModel().setChannelEnabled(NotificationChannelSpec.Announcements, false)

        coVerify(exactly = 0) { scheduler.rescheduleAll(any()) }
    }

    @Test
    fun `turning a channel off persists it`() {
        viewModel().setChannelEnabled(NotificationChannelSpec.ScheduleChanges, false)

        coVerify(exactly = 1) { preferencesStore.setChannelEnabled(NotificationChannelSpec.ScheduleChanges, false) }
    }

    private fun session(
        id: String,
        isBookmarked: Boolean,
    ) = Session(
        id = id,
        description = "",
        sessionFormat = "",
        sessionLevel = "",
        slug = id,
        title = id,
        endDateTime = "2026-11-08 10:00:00",
        endTime = "10:00 AM",
        isBookmarked = isBookmarked,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = "",
        startDateTime = "2026-11-08 09:00:00",
        startTime = "09:00 AM",
        rooms = "Room 1",
        speakers = listOf(),
        remoteId = id,
        eventDay = "1",
    )
}