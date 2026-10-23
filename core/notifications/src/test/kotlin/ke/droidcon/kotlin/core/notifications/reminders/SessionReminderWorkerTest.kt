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

import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferences
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionReminderWorkerTest {
    private fun worker(preferences: NotificationPreferences) =
        SessionReminderWorker(
            appContext = mockk(relaxed = true),
            params = mockk(relaxed = true),
            notificationPermission = mockk(relaxed = true),
            preferencesStore = mockk<NotificationPreferencesStore> { every { this@mockk.preferences } returns flowOf(preferences) },
        )

    @Test
    fun `a reminder that fires after session reminders were switched off is not posted`() = assertNotPosted(NotificationPreferences(sessionRemindersEnabled = false))

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