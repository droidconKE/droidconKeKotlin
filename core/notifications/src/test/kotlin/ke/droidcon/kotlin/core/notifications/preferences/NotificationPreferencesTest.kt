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
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationPreferencesTest {
    @Test
    fun isChannelEnabled_respectsMasterSwitch() {
        val preferences =
            NotificationPreferences(
                masterEnabled = false,
                sessionRemindersEnabled = true,
            )

        assertFalse(preferences.isChannelEnabled(NotificationChannelSpec.SessionReminders))
    }

    @Test
    fun isChannelEnabled_respectsChannelToggle() {
        val preferences =
            NotificationPreferences(
                masterEnabled = true,
                sessionRemindersEnabled = false,
                announcementsEnabled = true,
            )

        assertFalse(preferences.isChannelEnabled(NotificationChannelSpec.SessionReminders))
        assertTrue(preferences.isChannelEnabled(NotificationChannelSpec.Announcements))
    }

    @Test
    fun defaults_sessionRemindersOn_syncOff() {
        val preferences = NotificationPreferences()

        assertTrue(preferences.sessionRemindersEnabled)
        assertFalse(preferences.syncEnabled)
        assertTrue(preferences.reminderLeadTimeMinutes == 10)
    }

    @Test
    fun `preferences saved before phase 5 read as never prompted`() {
        val old = """{"masterEnabled":true,"reminderLeadTimeMinutes":10}"""

        assertFalse(Json.decodeFromString<NotificationPreferences>(old).permissionPrompted)
    }
}