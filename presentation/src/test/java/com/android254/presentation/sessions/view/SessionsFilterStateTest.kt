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
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers session filtering.
 *
 * Several of these fail against the previous implementation:
 *
 *  - the room tests, because filter values were hardcoded to "Room A"/"Room B"/"Room C"
 *    while sessions carry real venue room titles, and because the comparison was made
 *    against the comma-joined string rather than the individual rooms;
 *  - the case test, because the hardcoded value was "keynote" and the API returns
 *    "Keynote", compared with a case-sensitive `contains`.
 */
class SessionsFilterStateTest {
    @Test
    fun `empty filter state matches every session`() {
        assertTrue(SessionsFilterState().matches(session()))
    }

    @Test
    fun `room filter matches a real venue room name`() {
        val state = SessionsFilterState(rooms = listOf("Opal"))

        assertTrue(state.matches(session(rooms = "Opal")))
        assertFalse(state.matches(session(rooms = "Sapphire")))
    }

    @Test
    fun `room filter matches a session spanning several rooms`() {
        val state = SessionsFilterState(rooms = listOf("Opal"))

        // The API joins multiple rooms with a comma. Comparing against the whole joined
        // string matched neither room.
        assertTrue(state.matches(session(rooms = "Opal,Sapphire")))
        assertTrue(state.matches(session(rooms = "Sapphire, Opal")))
    }

    @Test
    fun `session type filter ignores case`() {
        val state = SessionsFilterState(sessionTypes = listOf("keynote"))

        assertTrue(state.matches(session(sessionFormat = "Keynote")))
    }

    @Test
    fun `level filter ignores surrounding whitespace on the session value`() {
        val state = SessionsFilterState(levels = listOf("Advanced"))

        assertTrue(state.matches(session(sessionLevel = " Advanced ")))
    }

    @Test
    fun `facets combine with and`() {
        val state = SessionsFilterState(rooms = listOf("Opal"), levels = listOf("Advanced"))

        assertTrue(state.matches(session(rooms = "Opal", sessionLevel = "Advanced")))
        assertFalse(state.matches(session(rooms = "Opal", sessionLevel = "Beginner")))
        assertFalse(state.matches(session(rooms = "Sapphire", sessionLevel = "Advanced")))
    }

    @Test
    fun `bookmark filter only keeps bookmarked sessions`() {
        val state = SessionsFilterState(isBookmarked = true)

        assertTrue(state.matches(session(isBookmarked = true)))
        assertFalse(state.matches(session(isBookmarked = false)))
    }

    @Test
    fun `toggling an option twice returns to the original state`() {
        val option =
            SessionsFilterOption(label = "Opal", value = "Opal", type = SessionsFilterCategory.Room)
        val state = SessionsFilterState()

        assertEquals(state, state.toggle(option).toggle(option))
    }

    @Test
    fun `isActive reflects whether any facet is set`() {
        assertFalse(SessionsFilterState().isActive)
        assertTrue(SessionsFilterState(rooms = listOf("Opal")).isActive)
        assertTrue(SessionsFilterState(isBookmarked = true).isActive)
    }

    private fun session(
        remoteId: String = "1",
        rooms: String = "Opal",
        sessionFormat: String = "Session",
        sessionLevel: String = "Advanced",
        isBookmarked: Boolean = false,
    ) = Session(
        id = remoteId,
        endDateTime = "2026-11-06T10:00:00",
        endTime = "10:00",
        isBookmarked = isBookmarked,
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