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
package com.android254.presentation.sessions.view

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.android254.presentation.common.calendar.CalendarEvent
import com.android254.presentation.common.calendar.addSessionToCalendarIntent
import com.android254.presentation.common.calendar.launchAddToCalendar
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.view.SessionDetailsScreen
import com.android254.presentation.sessionDetails.view.components.TestTag
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionsCalendarExportTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleEvent =
        CalendarEvent(
            id = "sess-1",
            title = "Keynote",
            description = "Opening keynote",
            venue = "Main Hall",
            speakers = persistentListOf("Jane Doe"),
            startEpochMillis = Instant.parse("2026-11-05T07:00:00Z").toEpochMilliseconds(),
            endEpochMillis = Instant.parse("2026-11-05T08:00:00Z").toEpochMilliseconds(),
        )

    private fun sampleDetails(
        id: String = sampleEvent.id,
        startEpochMillis: Long? = sampleEvent.startEpochMillis,
        endEpochMillis: Long? = sampleEvent.endEpochMillis,
        speakers: kotlinx.collections.immutable.ImmutableList<SessionDetailsSpeakerPresentationModel> =
            persistentListOf(SessionDetailsSpeakerPresentationModel("Jane Doe", "", "")),
    ) = SessionDetailsPresentationModel(
        id = id,
        title = sampleEvent.title,
        description = sampleEvent.description,
        venue = sampleEvent.venue,
        startTime = "10:00",
        endTime = "11:00 AM",
        amOrPm = "AM",
        isStarred = true,
        format = "Keynote",
        level = "All",
        sessionImageUrl = "",
        timeSlot = "10:00 - 11:00 AM",
        speakers = speakers,
        startEpochMillis = startEpochMillis,
        endEpochMillis = endEpochMillis,
    )

    @Test
    fun `session details calendar button invokes callback with event timestamps`() {
        var addedEvent: CalendarEvent? = null

        composeTestRule.setContent {
            ChaiTheme {
                SessionDetailsScreen(
                    uiState = SessionDetailsUiState.Success(sampleDetails()),
                    bookmarkSession = {},
                    unBookmarkSession = {},
                    onNavigationIconClick = {},
                    onAddToCalendar = { addedEvent = it },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTag.ADD_TO_CALENDAR_BUTTON).assertIsDisplayed().performClick()
        assertEquals(sampleEvent, addedEvent)
    }

    @Test
    fun `session details hides calendar button when timestamps are missing, invalid, or id is blank`() {
        var details by mutableStateOf(sampleDetails(startEpochMillis = null, endEpochMillis = null))

        composeTestRule.setContent {
            ChaiTheme {
                SessionDetailsScreen(
                    uiState = SessionDetailsUiState.Success(details),
                    bookmarkSession = {},
                    unBookmarkSession = {},
                    onNavigationIconClick = {},
                    onAddToCalendar = {},
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTag.ADD_TO_CALENDAR_BUTTON).assertDoesNotExist()

        details = sampleDetails(endEpochMillis = sampleEvent.startEpochMillis)
        composeTestRule.onNodeWithTag(TestTag.ADD_TO_CALENDAR_BUTTON).assertDoesNotExist()

        details = sampleDetails(id = "  ")
        composeTestRule.onNodeWithTag(TestTag.ADD_TO_CALENDAR_BUTTON).assertDoesNotExist()
    }

    @Test
    fun `tabletop session details renders and fires calendar button`() {
        var addedEvent: CalendarEvent? = null

        composeTestRule.setContent {
            ChaiTheme {
                SessionDetailsScreen(
                    uiState = SessionDetailsUiState.Success(sampleDetails()),
                    bookmarkSession = {},
                    unBookmarkSession = {},
                    onNavigationIconClick = {},
                    isTabletop = true,
                    onAddToCalendar = { addedEvent = it },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTag.ADD_TO_CALENDAR_BUTTON).assertIsDisplayed().performClick()
        assertEquals(sampleEvent, addedEvent)
    }

    @Test
    fun `service session without speakers renders and fires calendar button`() {
        var addedEvent: CalendarEvent? = null

        composeTestRule.setContent {
            ChaiTheme {
                SessionDetailsScreen(
                    uiState = SessionDetailsUiState.Success(sampleDetails(speakers = persistentListOf())),
                    bookmarkSession = {},
                    unBookmarkSession = {},
                    onNavigationIconClick = {},
                    onAddToCalendar = { addedEvent = it },
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTag.ADD_TO_CALENDAR_BUTTON).assertIsDisplayed().performClick()
        assertEquals(sampleEvent.copy(speakers = persistentListOf()), addedEvent)
    }

    @Test
    fun `session details renders snackbar message when calendar app is unavailable`() {
        val snackbarHostState = SnackbarHostState()
        val errorMessage = "No calendar app found on this device"

        composeTestRule.setContent {
            val scope = rememberCoroutineScope()
            ChaiTheme {
                SessionDetailsScreen(
                    uiState = SessionDetailsUiState.Success(sampleDetails()),
                    bookmarkSession = {},
                    unBookmarkSession = {},
                    onNavigationIconClick = {},
                    onAddToCalendar = {
                        scope.launch { snackbarHostState.showSnackbar(errorMessage) }
                    },
                    snackbarHostState = snackbarHostState,
                )
            }
        }

        composeTestRule.onNodeWithTag(TestTag.ADD_TO_CALENDAR_BUTTON).performClick()
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun `addSessionToCalendarIntent populates CalendarContract extras and omits unsupported reminder extra`() {
        val intent = addSessionToCalendarIntent(sampleEvent, speakersLine = "Speakers: Jane Doe")

        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(CalendarContract.Events.CONTENT_URI, intent.data)
        assertEquals(sampleEvent.title, intent.getStringExtra(CalendarContract.Events.TITLE))
        assertEquals(sampleEvent.venue, intent.getStringExtra(CalendarContract.Events.EVENT_LOCATION))
        assertEquals(sampleEvent.startEpochMillis, intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1L))
        assertEquals(sampleEvent.endEpochMillis, intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, -1L))
        assertEquals("Africa/Nairobi", intent.getStringExtra(CalendarContract.Events.EVENT_TIMEZONE))
        assertFalse(intent.hasExtra(CalendarContract.Reminders.MINUTES))
        val description = intent.getStringExtra(CalendarContract.Events.DESCRIPTION).orEmpty()
        assertTrue(description.contains("Speakers: Jane Doe"))
        assertTrue(description.contains("droidconke://session/sess-1"))
    }

    @Test
    fun `addSessionToCalendarIntent omits location extra when venue is blank`() {
        val intent = addSessionToCalendarIntent(sampleEvent.copy(venue = "   "))

        assertFalse(intent.hasExtra(CalendarContract.Events.EVENT_LOCATION))
    }

    @Test
    fun `launchAddToCalendar starts calendar activity with localized speakers line`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()

        val launched = launchAddToCalendar(activity, sampleEvent)

        assertTrue(launched)
        val started = shadowOf(activity).nextStartedActivity
        assertNotNull(started)
        assertEquals(Intent.ACTION_INSERT, started.action)
        assertEquals(0, started.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
        assertTrue(
            started
                .getStringExtra(CalendarContract.Events.DESCRIPTION)
                .orEmpty()
                .contains("Speakers: Jane Doe"),
        )
    }

    @Test
    fun `launchAddToCalendar adds FLAG_ACTIVITY_NEW_TASK for non-Activity contexts`() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        var capturedIntent: Intent? = null
        val recordingContext =
            object : ContextWrapper(appContext) {
                override fun startActivity(intent: Intent) {
                    capturedIntent = intent
                }
            }

        val launched = launchAddToCalendar(recordingContext, sampleEvent)

        assertTrue(launched)
        assertNotNull(capturedIntent)
        assertTrue((capturedIntent!!.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun `launchAddToCalendar returns false when no calendar app handles the intent`() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val throwingContext =
            object : ContextWrapper(appContext) {
                override fun startActivity(intent: Intent): Nothing = throw ActivityNotFoundException("No calendar app")
            }

        assertFalse(launchAddToCalendar(throwingContext, sampleEvent))
    }
}