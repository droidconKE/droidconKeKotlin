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

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import ke.droidcon.kotlin.core.notifications.NotificationPermission
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferences
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SessionReminderWorkerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun worker(
        preferences: NotificationPreferences,
        input: Data = Data.EMPTY,
    ) = SessionReminderWorker(
        appContext = context,
        params = mockk<WorkerParameters>(relaxed = true) { every { inputData } returns input },
        notificationPermission = mockk<NotificationPermission> { every { needsRuntimeRequest() } returns false },
        preferencesStore = mockk<NotificationPreferencesStore> { every { this@mockk.preferences } returns flowOf(preferences) },
    )

    @Test
    fun `a reminder that fires after session reminders were switched off is not posted`() = assertNotPosted(NotificationPreferences(sessionRemindersEnabled = false))

    @Test
    fun `a posted reminder records the start time it was shown for`() =
        runTest {
            val start = 1_793_862_000_000L
            val input =
                workDataOf(
                    SessionReminderWorker.KEY_SESSION_ID to "s1",
                    SessionReminderWorker.KEY_TITLE to "Welcome",
                    SessionReminderWorker.KEY_SESSION_START to start,
                )

            val result = worker(NotificationPreferences(), input).doWork()

            assertEquals(ListenableWorker.Result.success(workDataOf(SessionReminderWorker.KEY_REMINDED_START to start)), result)
            assertEquals(1, context.getSystemService(NotificationManager::class.java).activeNotifications.size)
        }

    private fun assertNotPosted(preferences: NotificationPreferences) =
        runTest {
            mockkStatic(NotificationManagerCompat::class)
            try {
                assertEquals(ListenableWorker.Result.success(), worker(preferences).doWork())
                verify(exactly = 0) { NotificationManagerCompat.from(any()) }
            } finally {
                unmockkStatic(NotificationManagerCompat::class)
            }
        }
}