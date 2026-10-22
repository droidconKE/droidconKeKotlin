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
package com.android254.data.repos

import com.android254.data.repos.mappers.toDomain
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.Ticket
import com.android254.domain.repos.AddTicketResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import ke.droidcon.kotlin.datasource.local.dao.TicketsDao
import ke.droidcon.kotlin.datasource.local.model.TicketEntity
import ke.droidcon.kotlin.datasource.remote.tickets.TicketsApi
import ke.droidcon.kotlin.datasource.remote.tickets.model.CheckInDTO
import ke.droidcon.kotlin.datasource.remote.tickets.model.IssuedTicketDTO
import ke.droidcon.kotlin.datasource.remote.tickets.model.TicketEventDTO
import ke.droidcon.kotlin.datasource.remote.tickets.model.TicketTypeDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

class TicketsManagerTest {
    private val api = mockk<TicketsApi>()
    private val dao = mockk<TicketsDao>(relaxed = true)
    private val clock =
        object : Clock {
            override fun now() = Instant.fromEpochMilliseconds(42)
        }
    private val manager = TicketsManager(api, dao, Dispatchers.Unconfined, clock)

    @Test
    fun `a number and the email it was bought with add the ticket, whatever the case`() =
        runTest {
            coEvery { api.findByNumber("TIC-70235") } returns DataResult.Success(listOf(issued))
            val saved = slot<TicketEntity>()
            coEvery { dao.replace(capture(saved)) } returns Unit

            val result = manager.add(" tic-70235", "MAGAK@example.com ")

            val ticket = (result as AddTicketResult.Added).ticket
            assertEquals("Magak Emmanuel", ticket.attendeeName)
            assertEquals(LocalDate(2026, 11, 5), ticket.firstDay)
            assertEquals(Ticket.Status.Active, ticket.status)
            assertEquals(listOf(Ticket.CheckIn(LocalDate(2026, 11, 5), Instant.parse("2026-11-05T05:41:12Z"))), ticket.checkIns)
            assertEquals("TIC-70235", saved.captured.number)
        }

    @Test
    fun `a number with someone else's email is not added`() =
        runTest {
            coEvery { api.findByNumber("TIC-70235") } returns DataResult.Success(listOf(issued))

            assertEquals(AddTicketResult.NoMatch, manager.add("TIC-70235", "someone@example.com"))
            coVerify(exactly = 0) { dao.replace(any()) }
        }

    @Test
    fun `a scanned ticket needs no email, and a code that is not a ticket asks nothing of the server`() =
        runTest {
            coEvery { api.findByNumber("TIC-70235") } returns DataResult.Success(listOf(issued))

            val scanned = manager.addScanned("https://events.droidcon.co.ke/checkin/droidconke-2026-731/attendee/TIC-70235")

            assertEquals("TIC-70235", (scanned as AddTicketResult.Added).ticket.number)
            assertEquals(AddTicketResult.NotATicket, manager.addScanned("https://droidcon.co.ke"))
            coVerify(exactly = 1) { api.findByNumber(any()) }
        }

    @Test
    fun `no signal, a busy network and a server failure each say so`() =
        runTest {
            coEvery { api.findByNumber("TIC-1") } returns DataResult.Error("Network error", networkError = true)
            coEvery { api.findByNumber("TIC-2") } returns DataResult.Error("Too many requests", rateLimited = true)
            coEvery { api.findByNumber("TIC-3") } returns DataResult.Error("Server error")

            assertEquals(AddTicketResult.Offline, manager.add("TIC-1", "a@b.c"))
            assertEquals(AddTicketResult.Busy, manager.add("TIC-2", "a@b.c"))
            assertEquals(AddTicketResult.Failed, manager.add("TIC-3", "a@b.c"))
        }

    @Test
    fun `refreshing updates the saved ticket in place`() =
        runTest {
            coEvery { dao.current() } returns issued.toEntity()
            coEvery { api.findByNumber("TIC-70235") } returns DataResult.Success(listOf(issued.copy(orderStatus = "refunded")))
            val saved = slot<TicketEntity>()
            coEvery { dao.update(capture(saved)) } returns Unit

            manager.refresh()

            assertEquals("refunded", saved.captured.status)
            coVerify(exactly = 0) { dao.replace(any()) }
        }

    @Test
    fun `a refresh that went through is not repeated for a few minutes, and a ticket the server no longer returns is kept`() =
        runTest {
            coEvery { dao.current() } returns issued.toEntity()
            coEvery { api.findByNumber("TIC-70235") } returns DataResult.Success(emptyList())

            manager.refresh()
            manager.refresh()

            coVerify(exactly = 1) { api.findByNumber(any()) }
            coVerify(exactly = 0) { dao.update(any()) }
            coVerify(exactly = 0) { dao.clear() }
        }

    @Test
    fun `a failed refresh is tried again next time`() =
        runTest {
            coEvery { dao.current() } returns issued.toEntity()
            coEvery { api.findByNumber("TIC-70235") } returns DataResult.Error("Network error", networkError = true)

            manager.refresh()
            manager.refresh()

            coVerify(exactly = 2) { api.findByNumber(any()) }
        }

    @Test
    fun `something that cannot be a ticket number costs no request`() =
        runTest {
            assertEquals(AddTicketResult.NoMatch, manager.add("my ticket", "a@b.c"))
            coVerify(exactly = 0) { api.findByNumber(any()) }
        }

    @Test
    fun `an order status the app does not know still shows its code, a missing one is valid`() =
        runTest {
            assertEquals(
                Ticket.Status.Unknown,
                issued
                    .copy(orderStatus = "pending")
                    .toEntity()
                    .toDomain()
                    .status,
            )
            assertEquals(
                Ticket.Status.Active,
                issued
                    .copy(orderStatus = null)
                    .toEntity()
                    .toDomain()
                    .status,
            )
            assertEquals(
                Ticket.Status.Active,
                issued
                    .copy(orderStatus = "paid")
                    .toEntity()
                    .toDomain()
                    .status,
            )
        }

    private val issued =
        IssuedTicketDTO(
            ticketNo = "TIC-70235",
            attendeeName = "Magak Emmanuel",
            attendeeEmail = "magak@example.com",
            ticket = TicketTypeDTO("Regular"),
            event = TicketEventDTO("DroidconKe & FlutterconKe 2026", "droidconke-2026-731", "2026-11-05T00:00:00+03:00", "2026-11-06", "PrideInn Azure", "Westlands, Nairobi"),
            orderStatus = "active",
            checkIns = listOf(CheckInDTO("2026-11-05", "2026-11-05T08:41:12+03:00")),
            qrBase64 = "iVBORw0KGgo=",
        )
}