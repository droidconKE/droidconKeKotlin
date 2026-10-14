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
package ke.droidcon.kotlin.core.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationChannelSpecTest {
    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(NotificationManager::class.java)!!
    }

    @Test
    fun createAll_createsAllSixChannels() {
        NotificationChannelSpec.createAll(context)

        val channels = notificationManager.notificationChannels
        val channelIds = channels.map { it.id }

        assertEquals("Should create exactly 6 channels", 6, channels.size)
        assertEquals(
            "Should have SessionReminders channel",
            true,
            "session_reminders" in channelIds,
        )
        assertEquals(
            "Should have ScheduleChanges channel",
            true,
            "schedule_changes" in channelIds,
        )
        assertEquals(
            "Should have Announcements channel",
            true,
            "announcements" in channelIds,
        )
        assertEquals(
            "Should have SocialFeed channel",
            true,
            "social_feed" in channelIds,
        )
        assertEquals(
            "Should have Milestones channel",
            true,
            "milestones" in channelIds,
        )
        assertEquals(
            "Should have Sync channel",
            true,
            "sync" in channelIds,
        )
    }

    @Test
    fun createAll_setsCorrectImportance() {
        NotificationChannelSpec.createAll(context)

        val channels = notificationManager.notificationChannels.associateBy { it.id }

        val sessionRemindersChannel = channels["session_reminders"]
        assertNotNull("SessionReminders channel should exist", sessionRemindersChannel)
        assertEquals(
            "SessionReminders should be IMPORTANCE_HIGH",
            NotificationManager.IMPORTANCE_HIGH,
            sessionRemindersChannel?.importance,
        )

        val syncChannel = channels["sync"]
        assertNotNull("Sync channel should exist", syncChannel)
        assertEquals(
            "Sync should be IMPORTANCE_MIN",
            NotificationManager.IMPORTANCE_MIN,
            syncChannel?.importance,
        )
    }

    @Test
    fun createAll_groupsChannels() {
        NotificationChannelSpec.createAll(context)

        val channels = notificationManager.notificationChannels
        val groups = channels.mapNotNull { it.group }.distinct()

        assertEquals("All channels should be in the same group", 1, groups.size)
        assertEquals("Group should be 'conference'", "conference", groups.firstOrNull())
    }
}
