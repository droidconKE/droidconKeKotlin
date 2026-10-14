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

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Instrumented tests for NotificationPreferencesStore using DataStore.
 *
 * Tests the persistence and retrieval of notification preferences across the store lifecycle.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NotificationPreferencesStoreTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var store: NotificationPreferencesStore

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun preferences_defaultsAreCorrect() =
        runTest {
            val prefs = store.preferences.first()

            assertTrue(prefs.masterEnabled, "Master should be enabled by default")
            assertTrue(prefs.sessionRemindersEnabled, "Session reminders should be enabled by default")
            assertTrue(prefs.scheduleChangesEnabled, "Schedule changes should be enabled by default")
            assertTrue(prefs.announcementsEnabled, "Announcements should be enabled by default")
            assertTrue(prefs.socialFeedEnabled, "Social feed should be enabled by default")
            assertTrue(prefs.milestonesEnabled, "Milestones should be enabled by default")
            assertFalse(prefs.syncEnabled, "Sync should be disabled by default")
            assertEquals(10, prefs.reminderLeadTimeMinutes, "Default reminder lead time should be 10 minutes")
        }

    @Test
    fun setMasterEnabled_persistsAndEmits() =
        runTest {
            store.setMasterEnabled(false)

            val prefs = store.preferences.first()
            assertFalse(prefs.masterEnabled, "Master enabled should be false after update")
        }

    @Test
    fun setChannelEnabled_updatesSingleChannel() =
        runTest {
            // Set SessionReminders to false while keeping others enabled
            store.setChannelEnabled(NotificationChannelSpec.SessionReminders, false)

            val prefs = store.preferences.first()

            assertFalse(prefs.sessionRemindersEnabled, "SessionReminders should be disabled")
            assertTrue(prefs.scheduleChangesEnabled, "ScheduleChanges should remain enabled")
            assertTrue(prefs.announcementsEnabled, "Announcements should remain enabled")
            assertTrue(prefs.socialFeedEnabled, "SocialFeed should remain enabled")
            assertTrue(prefs.milestonesEnabled, "Milestones should remain enabled")
            assertFalse(prefs.syncEnabled, "Sync should remain disabled")
        }

    @Test
    fun setReminderLeadTime_persists() =
        runTest {
            store.setReminderLeadTime(30)

            val prefs = store.preferences.first()
            assertEquals(30, prefs.reminderLeadTimeMinutes, "Reminder lead time should be 30 minutes after update")
        }
}