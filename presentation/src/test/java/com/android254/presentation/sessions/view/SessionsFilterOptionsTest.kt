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

import com.android254.domain.models.Session
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import io.mockk.mockk
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the filter options offered to the user.
 *
 * The first test is a property, not an example: an option matching nothing is a dead
 * filter, and asserting that none exist prevents the whole class of bug.
 */
class SessionsFilterOptionsTest {
    private val viewModel =
        SessionsViewModel(
            sessionsRepo = mockk(relaxed = true),
            syncDataWorkManager = mockk(relaxed = true),
            clock = Clock.System,
            conferenceTimeZone = TimeZone.of("Africa/Nairobi"),
        )

    private val sessions =
        listOf(
            session(remoteId = "1", rooms = "Opal", sessionFormat = "Keynote", sessionLevel = "Advanced"),
            session(remoteId = "2", rooms = "Sapphire,Opal", sessionFormat = "Workshop", sessionLevel = "Advanced"),
            session(remoteId = "3", rooms = "Amber", sessionFormat = "Session", sessionLevel = "Beginner"),
        )

    @Test
    fun `every offered option matches at least one session`() {
        val options = viewModel.buildFilterOptions(sessions)

        assertTrue("No filter options were derived", options.isNotEmpty())

        val dead =
            options.filterNot { option ->
                sessions.any { SessionsFilterState.from(option).matches(it) }
            }

        assertTrue(
            "These filter options match no session and would appear to do nothing: " +
                dead.joinToString { "${it.type}='${it.value}'" },
            dead.isEmpty(),
        )
    }

    @Test
    fun `rooms are derived individually from multi-room sessions`() {
        val rooms =
            viewModel
                .buildFilterOptions(sessions)
                .filter { it.type == SessionsFilterCategory.Room }
                .map { it.value }

        assertEquals(listOf("Amber", "Opal", "Sapphire"), rooms)
    }

    @Test
    fun `duplicate values collapse case-insensitively`() {
        val options =
            viewModel.buildFilterOptions(
                listOf(
                    session(remoteId = "1", sessionLevel = "Advanced"),
                    session(remoteId = "2", sessionLevel = "advanced"),
                    session(remoteId = "3", sessionLevel = "ADVANCED"),
                ),
            )

        assertEquals(1, options.count { it.type == SessionsFilterCategory.Level })
    }

    @Test
    fun `blank values are not offered`() {
        val options =
            viewModel.buildFilterOptions(
                listOf(session(remoteId = "1", rooms = "", sessionFormat = "  ", sessionLevel = "Advanced")),
            )

        assertTrue(options.none { it.value.isBlank() })
        assertEquals(0, options.count { it.type == SessionsFilterCategory.Room })
    }

    @Test
    fun `no options are derived from an empty session list`() {
        assertTrue(viewModel.buildFilterOptions(emptyList()).isEmpty())
    }

    private fun session(
        remoteId: String,
        rooms: String = "Opal",
        sessionFormat: String = "Session",
        sessionLevel: String = "Advanced",
    ) = Session(
        id = remoteId,
        endDateTime = "2026-11-06T10:00:00",
        endTime = "10:00",
        isBookmarked = false,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = null,
        startDateTime = "2026-11-06T09:00:00",
        startTime = "09:00",
        rooms = rooms,
        speakers = emptyList(),
        remoteId = remoteId,
        description = "",
        sessionFormat = sessionFormat,
        sessionLevel = sessionLevel,
        slug = "session-$remoteId",
        title = "Session $remoteId",
        eventDay = "06",
    )
}