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
package com.android254.presentation.ticket

import androidx.compose.runtime.Composable
import com.android254.domain.models.Ticket
import com.android254.presentation.ticket.view.TicketScreen
import ke.droidcon.kotlin.core.testing.testTicket
import ke.droidcon.kotlin.screenshot.ChaiScreenshotTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Test
import kotlin.time.Instant

class TicketScreenshotTest : ChaiScreenshotTest() {
    @Test
    fun `no ticket yet`() = captureScreen("screens/ticket_add") { Ticket() }

    @Test
    fun `a lookup that found nothing`() =
        captureScreen("screens/ticket_add_no_match") {
            Ticket(form = AddTicketForm(number = "TIC-70253", email = "magak@example.com", error = AddTicketError.NoMatch))
        }

    @Test
    fun `looking a ticket up`() = captureScreen("screens/ticket_adding") { Ticket(form = AddTicketForm(number = "TIC-70235", email = "magak@example.com", isAdding = true)) }

    @Test
    fun `a ticket ready for the door`() = captureScreen("screens/ticket") { Ticket(testTicket()) }

    @Test
    fun `checked in today`() =
        captureScreen("screens/ticket_checked_in") {
            Ticket(testTicket(checkIns = listOf(Ticket.CheckIn(LocalDate(2026, 11, 5), Instant.parse("2026-11-05T08:41:00+03:00")))))
        }

    @Test
    fun `a refunded ticket`() = captureScreen("screens/ticket_refunded") { Ticket(testTicket(status = Ticket.Status.Refunded)) }

    @Test
    fun `the ticket across form factors`() = captureFormFactors("form_factors/ticket") { Ticket(testTicket()) }
}

@Composable
private fun Ticket(
    ticket: Ticket? = null,
    form: AddTicketForm = AddTicketForm(),
) {
    TicketScreen(
        state =
            TicketUiState.Ready(
                ticket = ticket?.toUi(Instant.parse("2026-11-05T07:00:00Z"), TimeZone.of("Africa/Nairobi")),
                form = form,
            ),
        onBack = {},
        onAction = {},
    )
}