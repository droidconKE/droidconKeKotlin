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
package com.android254.presentation.home.mappers

import com.android254.domain.models.Home
import com.android254.domain.models.Ticket
import com.android254.presentation.home.viewstate.HomeTicket
import ke.droidcon.kotlin.core.testing.testSession
import ke.droidcon.kotlin.core.testing.testTicket
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class HomeTicketMappingTest {
    private val nairobi = TimeZone.of("Africa/Nairobi")
    private val home = Home(sessions = listOf(testSession("keynote", at("2026-11-05T09:00"), at("2026-11-05T10:00"))))

    @Test
    fun `with no ticket saved, Home invites one`() {
        assertEquals(HomeTicket.Add, home.toHomeState(isSyncing = false, now = at("2026-10-20T09:00"), zone = nairobi).ticket)
    }

    @Test
    fun `a saved ticket is on Home with its number and status`() {
        val ticket = home.toHomeState(isSyncing = false, now = at("2026-11-05T08:00"), zone = nairobi, ticket = testTicket(status = Ticket.Status.Refunded)).ticket

        assertEquals(HomeTicket.Saved("Magak Emmanuel", "Regular", "TIC-70235", Ticket.Status.Refunded), ticket)
    }

    @Test
    fun `the ticket leads Home from the morning of day one`() {
        assertEquals(false, home.toHomeState(isSyncing = false, now = at("2026-11-04T23:00"), zone = nairobi).isEventDay)
        assertEquals(true, home.toHomeState(isSyncing = false, now = at("2026-11-05T08:00"), zone = nairobi).isEventDay)
    }

    @Test
    fun `once the event is over the ticket leaves Home`() {
        val state = home.toHomeState(isSyncing = false, now = at("2026-11-08T09:00"), zone = nairobi, ticket = testTicket())

        assertEquals(HomeTicket.Hidden, state.ticket)
    }

    private fun at(nairobiTime: String) = Instant.parse("$nairobiTime:00+03:00")
}