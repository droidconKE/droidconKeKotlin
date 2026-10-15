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

import androidx.work.WorkManager
import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferences
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class SessionReminderSchedulerTest {
    private lateinit var workManager: WorkManager
    private lateinit var preferencesStore: NotificationPreferencesStore
    private lateinit var clock: Clock
    private lateinit var scheduler: SessionReminderScheduler

    @Before
    fun setUp() {
        workManager = mockk(relaxed = true)
        preferencesStore = mockk(relaxed = true)
        clock = mockk(relaxed = true)
        scheduler = SessionReminderScheduler(workManager, preferencesStore, clock)
    }

    private fun createSession(
        id: String = "session1",
        title: String = "Compose Workshop",
        startOffset: kotlin.time.Duration = 2.hours,
        room: String = "Room A",
        speakers: List<Speaker> = listOf(Speaker(name = "Alex")),
    ): Session {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        return Session(
            id = id,
            remoteId = id,
            title = title,
            description = "Test session",
            startInstant = now + startOffset,
            endInstant = now + startOffset + 1.hours,
            rooms = room,
            speakers = speakers,
            isBookmarked = true,
            endDateTime = "",
            endTime = "",
            isKeynote = false,
            isServiceSession = false,
            sessionImage = null,
            startDateTime = "",
            startTime = "",
            sessionFormat = "",
            sessionLevel = "",
            slug = "",
            eventDay = "",
        )
    }

    @Test
    fun rescheduleAll_cancelsExistingReminders() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        every { clock.now() } returns now
        every { preferencesStore.preferences } returns flowOf(NotificationPreferences())

        scheduler.rescheduleAll(emptyList())

        verify { workManager.cancelAllWorkByTag("session_reminder") }
    }

    @Test
    fun rescheduleAll_respectsDisabledPreference() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        every { clock.now() } returns now
        every { preferencesStore.preferences } returns flowOf(
            NotificationPreferences(sessionRemindersEnabled = false),
        )

        val session = createSession()
        scheduler.rescheduleAll(listOf(session))

        verify { workManager.cancelAllWorkByTag("session_reminder") }
    }

    @Test
    fun rescheduleAll_ignoresPastSessions() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        every { clock.now() } returns now
        every { preferencesStore.preferences } returns flowOf(NotificationPreferences())

        val pastSession = createSession(startOffset = (-1).hours)
        scheduler.rescheduleAll(listOf(pastSession))

        verify { workManager.cancelAllWorkByTag("session_reminder") }
    }

    @Test
    fun rescheduleAll_schedulesFutureSessionsWithLeadTime() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        every { clock.now() } returns now
        every { preferencesStore.preferences } returns flowOf(
            NotificationPreferences(reminderLeadTimeMinutes = 10),
        )

        val session = createSession(id = "session123", title = "Compose Workshop")
        scheduler.rescheduleAll(listOf(session))

        verify { workManager.cancelAllWorkByTag("session_reminder") }
    }

    @Test
    fun rescheduleAll_usesUniqueSessionIds() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        every { clock.now() } returns now
        every { preferencesStore.preferences } returns flowOf(NotificationPreferences())

        val session1 = createSession(id = "session1")
        val session2 = createSession(id = "session2", startOffset = 3.hours)

        scheduler.rescheduleAll(listOf(session1, session2))
        scheduler.rescheduleAll(listOf(session2))

        verify { workManager.cancelAllWorkByTag("session_reminder") }
    }

    @Test
    fun rescheduleAll_skipsRemindersWithPastFireTime() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        every { clock.now() } returns now
        every { preferencesStore.preferences } returns flowOf(
            NotificationPreferences(reminderLeadTimeMinutes = 15),
        )

        val session = createSession(startOffset = 10.minutes)
        scheduler.rescheduleAll(listOf(session))

        verify { workManager.cancelAllWorkByTag("session_reminder") }
    }

    @Test
    fun rescheduleAll_multipleSessionsAllScheduled() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        every { clock.now() } returns now
        every { preferencesStore.preferences } returns flowOf(NotificationPreferences())

        val sessions = listOf(
            createSession(id = "s1", title = "Session 1"),
            createSession(id = "s2", title = "Session 2", startOffset = 3.hours),
            createSession(id = "s3", title = "Session 3", startOffset = 5.hours),
        )

        scheduler.rescheduleAll(sessions)

        verify { workManager.cancelAllWorkByTag("session_reminder") }
    }
}
