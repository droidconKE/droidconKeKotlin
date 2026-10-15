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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationPreferencesStoreTest {
    private class FakeNotificationPreferencesStore : NotificationPreferencesStore {
        private var currentPreferences = NotificationPreferences()

        override val preferences: Flow<NotificationPreferences>
            get() = flowOf(currentPreferences)

        override suspend fun setMasterEnabled(enabled: Boolean) {
            setMasterEnabledSync(enabled)
        }

        override suspend fun setChannelEnabled(channel: NotificationChannelSpec, enabled: Boolean) {
            setChannelEnabledSync(channel, enabled)
        }

        override suspend fun setReminderLeadTime(minutes: Int) {
            setReminderLeadTimeSync(minutes)
        }

        fun setMasterEnabledSync(enabled: Boolean) {
            currentPreferences = currentPreferences.copy(masterEnabled = enabled)
        }

        fun setChannelEnabledSync(channel: NotificationChannelSpec, enabled: Boolean) {
            currentPreferences = when (channel) {
                NotificationChannelSpec.SessionReminders -> currentPreferences.copy(sessionRemindersEnabled = enabled)
                NotificationChannelSpec.ScheduleChanges -> currentPreferences.copy(scheduleChangesEnabled = enabled)
                NotificationChannelSpec.Announcements -> currentPreferences.copy(announcementsEnabled = enabled)
                NotificationChannelSpec.SocialFeed -> currentPreferences.copy(socialFeedEnabled = enabled)
                NotificationChannelSpec.Milestones -> currentPreferences.copy(milestonesEnabled = enabled)
                NotificationChannelSpec.Sync -> currentPreferences.copy(syncEnabled = enabled)
            }
        }

        fun setReminderLeadTimeSync(minutes: Int) {
            currentPreferences = currentPreferences.copy(reminderLeadTimeMinutes = minutes)
        }

        fun getCurrentPreferences(): NotificationPreferences = currentPreferences
    }

    private lateinit var store: FakeNotificationPreferencesStore

    private fun setUp() {
        store = FakeNotificationPreferencesStore()
    }

    @Test
    fun preferences_defaultsAreCorrect() {
        setUp()
        val prefs = store.getCurrentPreferences()

        assertTrue(prefs.masterEnabled, "Master should be enabled by default")
        assertTrue(prefs.sessionRemindersEnabled, "Session reminders should be enabled by default")
        assertTrue(prefs.scheduleChangesEnabled, "Schedule changes should be enabled by default")
        assertTrue(prefs.announcementsEnabled, "Announcements should be enabled by default")
        assertTrue(prefs.socialFeedEnabled, "Social feed should be enabled by default")
        assertTrue(prefs.milestonesEnabled, "Milestones should be enabled by default")
        assertFalse(prefs.syncEnabled, "Sync should be disabled by default")
        assertEquals(10, prefs.reminderLeadTimeMinutes, "Reminder lead time should default to 10 minutes")
    }

    @Test
    fun setMasterEnabled_true_persists() {
        setUp()
        store.setMasterEnabledSync(true)

        val prefs = store.getCurrentPreferences()
        assertTrue(prefs.masterEnabled)
    }

    @Test
    fun setMasterEnabled_false_persists() {
        setUp()
        store.setMasterEnabledSync(false)

        val prefs = store.getCurrentPreferences()
        assertFalse(prefs.masterEnabled)
    }

    @Test
    fun setChannelEnabled_sessionReminders_persists() {
        setUp()
        store.setChannelEnabledSync(NotificationChannelSpec.SessionReminders, false)

        val prefs = store.getCurrentPreferences()
        assertFalse(prefs.sessionRemindersEnabled)
        assertTrue(prefs.announcementsEnabled, "Other channels should remain unchanged")
    }

    @Test
    fun setChannelEnabled_scheduleChanges_persists() {
        setUp()
        store.setChannelEnabledSync(NotificationChannelSpec.ScheduleChanges, false)

        val prefs = store.getCurrentPreferences()
        assertFalse(prefs.scheduleChangesEnabled)
        assertTrue(prefs.sessionRemindersEnabled, "Other channels should remain unchanged")
    }

    @Test
    fun setChannelEnabled_announcements_persists() {
        setUp()
        store.setChannelEnabledSync(NotificationChannelSpec.Announcements, false)

        val prefs = store.getCurrentPreferences()
        assertFalse(prefs.announcementsEnabled)
        assertTrue(prefs.milestonesEnabled, "Other channels should remain unchanged")
    }

    @Test
    fun setChannelEnabled_socialFeed_persists() {
        setUp()
        store.setChannelEnabledSync(NotificationChannelSpec.SocialFeed, false)

        val prefs = store.getCurrentPreferences()
        assertFalse(prefs.socialFeedEnabled)
        assertTrue(prefs.milestonesEnabled, "Other channels should remain unchanged")
    }

    @Test
    fun setChannelEnabled_milestones_persists() {
        setUp()
        store.setChannelEnabledSync(NotificationChannelSpec.Milestones, false)

        val prefs = store.getCurrentPreferences()
        assertFalse(prefs.milestonesEnabled)
        assertTrue(prefs.sessionRemindersEnabled, "Other channels should remain unchanged")
    }

    @Test
    fun setChannelEnabled_sync_persists() {
        setUp()
        store.setChannelEnabledSync(NotificationChannelSpec.Sync, true)

        val prefs = store.getCurrentPreferences()
        assertTrue(prefs.syncEnabled)
        assertTrue(prefs.sessionRemindersEnabled, "Other channels should remain unchanged")
    }

    @Test
    fun setReminderLeadTime_5_persists() {
        setUp()
        store.setReminderLeadTimeSync(5)

        val prefs = store.getCurrentPreferences()
        assertEquals(5, prefs.reminderLeadTimeMinutes)
    }

    @Test
    fun setReminderLeadTime_15_persists() {
        setUp()
        store.setReminderLeadTimeSync(15)

        val prefs = store.getCurrentPreferences()
        assertEquals(15, prefs.reminderLeadTimeMinutes)
    }

    @Test
    fun setReminderLeadTime_30_persists() {
        setUp()
        store.setReminderLeadTimeSync(30)

        val prefs = store.getCurrentPreferences()
        assertEquals(30, prefs.reminderLeadTimeMinutes)
    }

    @Test
    fun multipleUpdates_allPersist() {
        setUp()
        store.setMasterEnabledSync(true)
        store.setChannelEnabledSync(NotificationChannelSpec.SessionReminders, false)
        store.setChannelEnabledSync(NotificationChannelSpec.Sync, true)
        store.setReminderLeadTimeSync(20)

        val prefs = store.getCurrentPreferences()
        assertTrue(prefs.masterEnabled)
        assertFalse(prefs.sessionRemindersEnabled)
        assertTrue(prefs.syncEnabled)
        assertEquals(20, prefs.reminderLeadTimeMinutes)
    }

    @Test
    fun isChannelEnabled_respectsMasterSwitch() {
        setUp()
        store.setMasterEnabledSync(false)
        store.setChannelEnabledSync(NotificationChannelSpec.SessionReminders, true)

        val prefs = store.getCurrentPreferences()
        assertFalse(
            prefs.isChannelEnabled(NotificationChannelSpec.SessionReminders),
            "Channel should be disabled when master is off, regardless of channel toggle"
        )
    }

    @Test
    fun isChannelEnabled_respectsChannelToggle() {
        setUp()
        store.setMasterEnabledSync(true)
        store.setChannelEnabledSync(NotificationChannelSpec.SessionReminders, false)

        val prefs = store.getCurrentPreferences()
        assertFalse(
            prefs.isChannelEnabled(NotificationChannelSpec.SessionReminders),
            "Channel should be disabled when its toggle is off"
        )
    }

    @Test
    fun isChannelEnabled_enabledWhenBothOn() {
        setUp()
        store.setMasterEnabledSync(true)
        store.setChannelEnabledSync(NotificationChannelSpec.SessionReminders, true)

        val prefs = store.getCurrentPreferences()
        assertTrue(
            prefs.isChannelEnabled(NotificationChannelSpec.SessionReminders),
            "Channel should be enabled when both master and channel toggle are on"
        )
    }
}
