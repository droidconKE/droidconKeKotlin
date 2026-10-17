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

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.activity.MainActivity
import com.android254.presentation.common.navigation.DeepLinks
import com.android254.presentation.common.navigation.Screens
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferences
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import ke.droidcon.kotlin.core.notifications.push.PushPayload
import ke.droidcon.kotlin.core.notifications.push.PushPayload.ScheduleChange.ChangeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PushPayloadHandlerTest {
    private val context: Application = ApplicationProvider.getApplicationContext()
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val preferences = MutableStateFlow(NotificationPreferences())
    private val preferencesStore = mockk<NotificationPreferencesStore> { every { preferences } returns this@PushPayloadHandlerTest.preferences }
    private val syncDataWorkManager = mockk<SyncDataWorkManager>(relaxed = true)
    private val handler = PushPayloadHandlerImpl(context, preferencesStore, syncDataWorkManager, AndroidNotificationPermission(context))

    @Before
    fun setUp() {
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        NotificationChannelSpec.createAll(context)
    }

    private fun posted(): List<Notification> = shadowOf(notificationManager).allNotifications

    private fun Notification.title() = extras.getCharSequence(NotificationCompat.EXTRA_TITLE)?.toString()

    private fun Notification.text() = extras.getCharSequence(NotificationCompat.EXTRA_TEXT)?.toString()

    private fun Notification.tapIntent() = shadowOf(contentIntent).savedIntent

    @Test
    fun `a schedule change also starts a sync so reminders move with the session`() =
        runTest {
            handler.handle(PushPayload.ScheduleChange("s1", ChangeType.TimeChanged, "Now at 2 PM"))

            verify(exactly = 1) { syncDataWorkManager.startSync() }
        }

    @Test
    fun `a schedule change with its channel off still syncs`() =
        runTest {
            preferences.value = NotificationPreferences(scheduleChangesEnabled = false)

            handler.handle(PushPayload.ScheduleChange("s1", ChangeType.Cancelled, "Cancelled"))

            verify(exactly = 1) { syncDataWorkManager.startSync() }
            assertTrue(posted().isEmpty())
        }

    @Test
    fun `a schedule change posts on the schedule changes channel and opens the session`() =
        runTest {
            handler.handle(PushPayload.ScheduleChange("s1", ChangeType.RoomChanged, "Moved to Room B"))

            val notification = posted().single()
            assertEquals(NotificationChannelSpec.ScheduleChanges.id, notification.channelId)
            assertEquals("Room changed", notification.title())
            assertEquals("Moved to Room B", notification.text())
            assertEquals(DeepLinks.session("s1"), notification.tapIntent().dataString)
        }

    @Test
    fun `each change type has its own title`() =
        runTest {
            ChangeType.entries.forEach { type ->
                handler.handle(PushPayload.ScheduleChange("s-$type", type, "msg"))
            }

            assertEquals(
                setOf("Room changed", "Time changed", "Session cancelled", "Speaker changed"),
                posted().map { it.title() }.toSet(),
            )
        }

    @Test
    fun `a schedule change does not replace the reminder for the same session`() =
        runTest {
            val reminderId = "s1".hashCode()
            notificationManager.notify(
                reminderId,
                NotificationCompat
                    .Builder(context, NotificationChannelSpec.SessionReminders.id)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .build(),
            )

            handler.handle(PushPayload.ScheduleChange("s1", ChangeType.TimeChanged, "Now at 2 PM"))

            assertEquals(2, posted().size)
        }

    @Test
    fun `an announcement with an app link opens that destination`() =
        runTest {
            handler.handle(PushPayload.Announcement("Lunch", "Served in the main hall", DeepLinks.feed()))

            val notification = posted().single()
            assertEquals(NotificationChannelSpec.Announcements.id, notification.channelId)
            assertEquals("Lunch", notification.title())
            assertEquals("Served in the main hall", notification.text())
            assertEquals(DeepLinks.feed(), notification.tapIntent().dataString)
        }

    @Test
    fun `an announcement with no link or a foreign link opens the app`() =
        runTest {
            handler.handle(PushPayload.Announcement("A", "no link"))
            handler.handle(PushPayload.Announcement("B", "foreign link", "https://example.com"))

            assertEquals(2, posted().size)
            posted().forEach { notification ->
                assertNull(notification.tapIntent().data)
                assertEquals(MainActivity::class.java.name, notification.tapIntent().component?.className)
            }
        }

    @Test
    fun `a feed post posts on the social feed channel and opens the feed`() =
        runTest {
            handler.handle(PushPayload.NewFeedPost("p1", "Great keynote!"))

            val notification = posted().single()
            assertEquals(NotificationChannelSpec.SocialFeed.id, notification.channelId)
            assertEquals("New post", notification.title())
            assertEquals("Great keynote!", notification.text())
            assertEquals(Screens.Feed, notification.tapIntent().dataString?.let(DeepLinks::parse))
        }

    @Test
    fun `a milestone posts on the milestones channel`() =
        runTest {
            handler.handle(PushPayload.Milestone("That's a wrap", "See you next year"))

            val notification = posted().single()
            assertEquals(NotificationChannelSpec.Milestones.id, notification.channelId)
            assertEquals("That's a wrap", notification.title())
        }

    @Test
    fun `a sync request starts a sync and posts nothing`() =
        runTest {
            handler.handle(PushPayload.SyncRequest)

            verify(exactly = 1) { syncDataWorkManager.startSync() }
            assertTrue(posted().isEmpty())
        }

    @Test
    fun `a sync request still syncs when every notification is off`() =
        runTest {
            preferences.value = NotificationPreferences(masterEnabled = false)

            handler.handle(PushPayload.SyncRequest)

            verify(exactly = 1) { syncDataWorkManager.startSync() }
        }

    @Test
    fun `a disabled channel posts nothing`() =
        runTest {
            preferences.value = NotificationPreferences(scheduleChangesEnabled = false)

            handler.handle(PushPayload.ScheduleChange("s1", ChangeType.Cancelled, "Cancelled"))
            handler.handle(PushPayload.Announcement("Still", "posted"))

            assertEquals(listOf(NotificationChannelSpec.Announcements.id), posted().map { it.channelId })
        }

    @Test
    fun `the master switch off posts nothing`() =
        runTest {
            preferences.value = NotificationPreferences(masterEnabled = false)

            handler.handle(PushPayload.Announcement("Lunch", "Served"))
            handler.handle(PushPayload.NewFeedPost("p1", "hi"))
            handler.handle(PushPayload.Milestone("m", "b"))

            assertTrue(posted().isEmpty())
        }

    @Test
    fun `without the notification permission nothing is posted`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

            handler.handle(PushPayload.Announcement("Lunch", "Served"))

            assertTrue(posted().isEmpty())
        }
}