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
package ke.droidcon.kotlin.core.notifications.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationPreferencesImplTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun TestScope.store() =
        NotificationPreferencesImpl(
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { folder.newFile("notifications.preferences_pb") },
            ),
        )

    @Test
    fun `nothing stored reads as the defaults`() =
        runTest(UnconfinedTestDispatcher()) {
            assertEquals(NotificationPreferences(), store().preferences.first())
        }

    @Test
    fun `a channel switch persists`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = store()

            store.setChannelEnabled(NotificationChannelSpec.SessionReminders, false)

            val prefs = store.preferences.first()
            assertFalse(prefs.sessionRemindersEnabled)
            assertTrue(prefs.announcementsEnabled)
        }

    @Test
    fun `the sync channel has no switch to persist`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = store()

            store.setChannelEnabled(NotificationChannelSpec.Sync, false)

            assertEquals(NotificationPreferences(), store.preferences.first())
        }

    @Test
    fun `lead time and the prompted flag persist`() =
        runTest(UnconfinedTestDispatcher()) {
            val store = store()

            store.setReminderLeadTime(30)
            store.markPermissionPrompted()

            val prefs = store.preferences.first()
            assertEquals(30, prefs.reminderLeadTimeMinutes)
            assertTrue(prefs.permissionPrompted)
        }
}
