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
package ke.droidcon.kotlin.core.notifications.reminders

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.android254.domain.models.Session
import com.google.common.util.concurrent.Futures
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferences
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class SessionReminderSchedulerTest {
    private val now = Instant.parse("2026-11-08T09:00:00Z")
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val preferencesStore = mockk<NotificationPreferencesStore>()
    private val clock = mockk<Clock> { every { now() } returns now }

    private fun TestScope.scheduler(
        preferences: NotificationPreferences = NotificationPreferences(),
        pendingSessionIds: List<String> = emptyList(),
    ): SessionReminderScheduler {
        every { preferencesStore.preferences } returns flowOf(preferences)
        val pending =
            pendingSessionIds.map { id ->
                mockk<WorkInfo> {
                    every { state } returns WorkInfo.State.ENQUEUED
                    every { tags } returns setOf("session_reminder", "session_reminder_for:$id")
                }
            }
        every { workManager.getWorkInfosByTag("session_reminder") } returns Futures.immediateFuture(pending)
        return SessionReminderScheduler(workManager, preferencesStore, clock, StandardTestDispatcher(testScheduler))
    }

    @Test
    fun `switching session reminders off cancels the scheduled work`() =
        runTest {
            scheduler(NotificationPreferences(sessionRemindersEnabled = false)).rescheduleAll(listOf(session("s1")))

            verify(exactly = 1) { workManager.cancelAllWorkByTag("session_reminder") }
            verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
        }

    @Test
    fun `rescheduling never cancels everything, so a reminder that is due survives`() =
        runTest {
            // Fires at 08:55, five minutes ago: already due and possibly running.
            scheduler(NotificationPreferences(reminderLeadTimeMinutes = 10), pendingSessionIds = listOf("due"))
                .rescheduleAll(listOf(session("due", startsIn = 5.minutes)))

            verify(exactly = 0) { workManager.cancelAllWorkByTag(any()) }
            verify(exactly = 0) { workManager.cancelUniqueWork(any()) }
            verify(exactly = 0) { workManager.enqueueUniqueWork(any(), any(), any<androidx.work.OneTimeWorkRequest>()) }
        }

    @Test
    fun `a reminder for a session that was unstarred is cancelled`() =
        runTest {
            scheduler(pendingSessionIds = listOf("kept", "unstarred"))
                .rescheduleAll(listOf(session("kept")))

            verify(exactly = 1) { workManager.cancelUniqueWork("reminder_unstarred") }
            verify(exactly = 0) { workManager.cancelUniqueWork("reminder_kept") }
        }

    @Test
    fun `a future reminder is scheduled once per session under a stable name`() =
        runTest {
            scheduler().rescheduleAll(listOf(session("s1"), session("s2", startsIn = 3.hours)))

            verify(exactly = 1) {
                workManager.enqueueUniqueWork("reminder_s1", ExistingWorkPolicy.REPLACE, any<androidx.work.OneTimeWorkRequest>())
            }
            verify(exactly = 1) {
                workManager.enqueueUniqueWork("reminder_s2", ExistingWorkPolicy.REPLACE, any<androidx.work.OneTimeWorkRequest>())
            }
        }

    private fun session(
        id: String,
        startsIn: Duration = 2.hours,
    ) = Session(
        id = id,
        description = "",
        sessionFormat = "",
        sessionLevel = "",
        slug = id,
        title = id,
        endDateTime = "",
        endTime = "",
        isBookmarked = true,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = "",
        startDateTime = "",
        startTime = "",
        rooms = "Sapphire,Opal",
        speakers = listOf(),
        remoteId = id,
        eventDay = "",
        startInstant = now + startsIn,
        endInstant = now + startsIn + 1.hours,
    )
}
