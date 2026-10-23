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

import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationPreferencesTest {
    @Test
    fun `each channel follows its own switch`() {
        val preferences = NotificationPreferences(sessionRemindersEnabled = false, announcementsEnabled = true)

        assertFalse(preferences.isChannelEnabled(NotificationChannelSpec.SessionReminders))
        assertTrue(preferences.isChannelEnabled(NotificationChannelSpec.Announcements))
    }

    @Test
    fun `the sync channel is never switched off`() {
        assertTrue(NotificationPreferences().isChannelEnabled(NotificationChannelSpec.Sync))
    }

    @Test
    fun `defaults remind ten minutes ahead and have never prompted`() {
        val preferences = NotificationPreferences()

        assertTrue(preferences.sessionRemindersEnabled)
        assertEquals(10, preferences.reminderLeadTimeMinutes)
        assertFalse(preferences.permissionPrompted)
    }
}