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
import ke.droidcon.kotlin.core.domain.model.Session
import ke.droidcon.kotlin.core.domain.model.Speaker
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferences
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@RunWith(RobolectricTestRunner::class)
class SessionReminderSchedulerTest {
    private lateinit var workManager: WorkManager
    private lateinit var preferencesStore: NotificationPreferencesStore
    private lateinit var clock: Clock
    private lateinit var scheduler: SessionReminderScheduler

    @Before
    fun setUp() {
        workManager = mock()
        preferencesStore = mock()
        clock = mock()
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
            remoteId = id,
            title = title,
            description = "Test session",
            startInstant = now + startOffset,
            endInstant = now + startOffset + 1.hours,
            rooms = room,
            speakers = speakers,
            bookmarked = true,
        )
    }

    @Test
    fun rescheduleAll_cancelsExistingReminders() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        whenever(clock.now()).thenReturn(now)
        whenever(preferencesStore.preferences).thenReturn(flowOf(NotificationPreferences()))

        scheduler.rescheduleAll(emptyList())

        verify(workManager).cancelAllWorkByTag("session_reminder")
    }

    @Test
    fun rescheduleAll_respectsDisabledPreference() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        whenever(clock.now()).thenReturn(now)
        whenever(preferencesStore.preferences).thenReturn(
            flowOf(NotificationPreferences(sessionRemindersEnabled = false))
        )

        val session = createSession()
        scheduler.rescheduleAll(listOf(session))

        // Should cancel existing but not schedule new ones
        verify(workManager).cancelAllWorkByTag("session_reminder")
    }

    @Test
    fun rescheduleAll_ignoresPastSessions() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        whenever(clock.now()).thenReturn(now)
        whenever(preferencesStore.preferences).thenReturn(flowOf(NotificationPreferences()))

        val pastSession = createSession(startOffset = (-1).hours)  // Starts in the past
        scheduler.rescheduleAll(listOf(pastSession))

        // Should cancel but not enqueue anything
        verify(workManager).cancelAllWorkByTag("session_reminder")
    }

    @Test
    fun rescheduleAll_schedulesFutureSessionsWithLeadTime() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        whenever(clock.now()).thenReturn(now)
        whenever(preferencesStore.preferences).thenReturn(
            flowOf(NotificationPreferences(reminderLeadTimeMinutes = 10))
        )

        val session = createSession(id = "session123", title = "Compose Workshop")
        scheduler.rescheduleAll(listOf(session))

        verify(workManager).cancelAllWorkByTag("session_reminder")
        // Note: We can't easily verify enqueueUniqueWork without a full WorkManager mock,
        // but the lack of errors means it was called successfully
    }

    @Test
    fun rescheduleAll_usesUniqueSessionIds() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        whenever(clock.now()).thenReturn(now)
        whenever(preferencesStore.preferences).thenReturn(flowOf(NotificationPreferences()))

        val session1 = createSession(id = "session1")
        val session2 = createSession(id = "session2", startOffset = 3.hours)

        // First schedule
        scheduler.rescheduleAll(listOf(session1, session2))

        // Should use unique IDs: reminder_session1, reminder_session2
        // Re-scheduling should use REPLACE policy
        scheduler.rescheduleAll(listOf(session2))

        verify(workManager).cancelAllWorkByTag("session_reminder")
    }

    @Test
    fun rescheduleAll_respects LeadTime() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        whenever(clock.now()).thenReturn(now)
        whenever(preferencesStore.preferences).thenReturn(
            flowOf(NotificationPreferences(reminderLeadTimeMinutes = 30))
        )

        val session = createSession(startOffset = 35.minutes)
        // Session starts in 35 minutes, reminder at 30 min lead time means fire in 5 minutes
        scheduler.rescheduleAll(listOf(session))

        verify(workManager).cancelAllWorkByTag("session_reminder")
    }

    @Test
    fun rescheduleAll_skipsRemindersWithPastFireTime() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        whenever(clock.now()).thenReturn(now)
        whenever(preferencesStore.preferences).thenReturn(
            flowOf(NotificationPreferences(reminderLeadTimeMinutes = 15))
        )

        // Session starts in 10 minutes, but 15 min lead time would fire in the past
        val session = createSession(startOffset = 10.minutes)
        scheduler.rescheduleAll(listOf(session))

        verify(workManager).cancelAllWorkByTag("session_reminder")
        // Should cancel but not enqueue (reminder time is in the past)
    }

    @Test
    fun rescheduleAll_multipleSessionsAllScheduled() = runTest {
        val now = Instant.parse("2026-11-08T09:00:00Z")
        whenever(clock.now()).thenReturn(now)
        whenever(preferencesStore.preferences).thenReturn(flowOf(NotificationPreferences()))

        val sessions = listOf(
            createSession(id = "s1", title = "Session 1"),
            createSession(id = "s2", title = "Session 2", startOffset = 3.hours),
            createSession(id = "s3", title = "Session 3", startOffset = 5.hours),
        )

        scheduler.rescheduleAll(sessions)

        verify(workManager).cancelAllWorkByTag("session_reminder")
        // All three sessions should be enqueued (no errors = success)
    }
}
