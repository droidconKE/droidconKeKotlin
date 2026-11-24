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

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android254.presentation.common.calendar.CalendarEvent
import com.android254.presentation.common.calendar.addSessionToCalendarIntent
import com.android254.presentation.common.fakedata.DAY_TODAY
import com.android254.presentation.common.fakedata.fakeSessions
import com.android254.presentation.common.resultstatus.ResultStatus
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.models.SessionDetailsSpeakerPresentationModel
import com.android254.presentation.sessionDetails.SessionDetailsUiState
import com.android254.presentation.sessionDetails.view.SessionDetailsScreen
import com.android254.presentation.sessions.models.SessionsUiState
import com.droidconke.chai.ChaiTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionsCalendarExportTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val starredEvent =
        CalendarEvent(
            id = "sess-1",
            title = "Keynote",
            description = "Opening keynote",
            venue = "Main Hall",
            speakers = persistentListOf("Jane Doe"),
            startEpochMillis = Instant.parse("2026-11-05T07:00:00Z").toEpochMilliseconds(),
            endEpochMillis = Instant.parse("2026-11-05T08:00:00Z").toEpochMilliseconds(),
        )

    @Test
    fun `export button appears only in My sessions and exports all starred events`() {
        var exported: List<CalendarEvent> = emptyList()
        val grouped =
            fakeSessions
                .take(1)
                .groupBy { "${it.startTime} ${it.amOrPm}" }
                .mapValues { it.value.toImmutableList() }
                .toImmutableMap()

        composeTestRule.setContent {
            ChaiTheme {
                SessionsScreen(
                    sessionsUiState =
                        SessionsUiState(
                            sessions = grouped,
                            eventDays = persistentListOf(EventDate(DAY_TODAY, day = 1)),
                            sessionStatus = ResultStatus.Success,
                            showMySessionsOnly = true,
                            starredCalendarEvents = persistentListOf(starredEvent),
                        ),
                    selectedEventDate = EventDate(DAY_TODAY, day = 1),
                    isRefreshing = false,
                    currentSelections = persistentListOf(),
                    navigateToSessionDetails = {},
                    onEvent = {},
                    onExportAgenda = { exported = it },
                )
            }
        }

        composeTestRule.onNodeWithTag("export_agenda_button").assertIsDisplayed().performClick()
        assertEquals(listOf(starredEvent), exported)
    }

    @Test
    fun `export button is hidden when viewing All sessions`() {
        val grouped =
            fakeSessions
                .take(1)
                .groupBy { "${it.startTime} ${it.amOrPm}" }
                .mapValues { it.value.toImmutableList() }
                .toImmutableMap()

        composeTestRule.setContent {
            ChaiTheme {
                SessionsScreen(
                    sessionsUiState =
                        SessionsUiState(
                            sessions = grouped,
                            eventDays = persistentListOf(EventDate(DAY_TODAY, day = 1)),
                            sessionStatus = ResultStatus.Success,
                            showMySessionsOnly = false,
                            starredCalendarEvents = persistentListOf(starredEvent),
                        ),
                    selectedEventDate = EventDate(DAY_TODAY, day = 1),
                    isRefreshing = false,
                    currentSelections = persistentListOf(),
                    navigateToSessionDetails = {},
                    onEvent = {},
                    onExportAgenda = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("export_agenda_button").assertDoesNotExist()
    }

    @Test
    fun `session details calendar button invokes callback with event timestamps`() {
        var addedEvent: CalendarEvent? = null
        val details =
            SessionDetailsPresentationModel(
                id = "sess-1",
                title = "Keynote",
                description = "Opening keynote",
                venue = "Main Hall",
                startTime = "10:00",
                endTime = "11:00 AM",
                amOrPm = "AM",
                isStarred = true,
                format = "Keynote",
                level = "All",
                sessionImageUrl = "",
                timeSlot = "10:00 - 11:00 AM",
                speakers = persistentListOf(SessionDetailsSpeakerPresentationModel("Jane Doe", "", "")),
                startEpochMillis = starredEvent.startEpochMillis,
                endEpochMillis = starredEvent.endEpochMillis,
            )

        composeTestRule.setContent {
            ChaiTheme {
                SessionDetailsScreen(
                    uiState = SessionDetailsUiState.Success(details),
                    bookmarkSession = {},
                    unBookmarkSession = {},
                    onNavigationIconClick = {},
                    onAddToCalendar = { addedEvent = it },
                )
            }
        }

        composeTestRule.onNodeWithTag("add_to_calendar_button").assertIsDisplayed().performClick()
        assertEquals(starredEvent, addedEvent)
    }

    @Test
    fun `addSessionToCalendarIntent populates CalendarContract extras and Nairobi timezone`() {
        val intent = addSessionToCalendarIntent(starredEvent)

        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(CalendarContract.Events.CONTENT_URI, intent.data)
        assertEquals(starredEvent.title, intent.getStringExtra(CalendarContract.Events.TITLE))
        assertEquals(starredEvent.venue, intent.getStringExtra(CalendarContract.Events.EVENT_LOCATION))
        assertEquals(starredEvent.startEpochMillis, intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, -1L))
        assertEquals(starredEvent.endEpochMillis, intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, -1L))
        assertEquals("Africa/Nairobi", intent.getStringExtra(CalendarContract.Events.EVENT_TIMEZONE))
        assertEquals(10, intent.getIntExtra(CalendarContract.Reminders.MINUTES, -1))
        val description = intent.getStringExtra(CalendarContract.Events.DESCRIPTION).orEmpty()
        assertTrue(description.contains("Speakers: Jane Doe"))
        assertTrue(description.contains("droidconke://session/sess-1"))
    }
}