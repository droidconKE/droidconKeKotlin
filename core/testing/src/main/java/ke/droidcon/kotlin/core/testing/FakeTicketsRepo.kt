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
package ke.droidcon.kotlin.core.testing

import com.android254.domain.models.Ticket
import com.android254.domain.repos.AddTicketResult
import com.android254.domain.repos.TicketsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate

class FakeTicketsRepo(
    ticket: Ticket? = null,
    var nextResult: AddTicketResult? = null,
) : TicketsRepo {
    val saved = MutableStateFlow(ticket)
    var refreshes = 0
        private set
    var lastLookUp: Pair<String, String?>? = null
        private set

    override fun ticket(): Flow<Ticket?> = saved

    override suspend fun add(
        number: String,
        email: String,
    ): AddTicketResult = respond(number to email)

    override suspend fun addScanned(code: String): AddTicketResult = respond(code to null)

    override suspend fun refresh() {
        refreshes++
    }

    override suspend fun remove() {
        saved.value = null
    }

    private fun respond(lookUp: Pair<String, String?>): AddTicketResult {
        lastLookUp = lookUp
        val result = nextResult ?: AddTicketResult.Added(testTicket(number = lookUp.first))
        if (result is AddTicketResult.Added) saved.value = result.ticket
        return result
    }
}

fun testTicket(
    number: String = "TIC-70235",
    attendeeName: String = "Magak Emmanuel",
    type: String = "Regular",
    status: Ticket.Status = Ticket.Status.Active,
    checkIns: List<Ticket.CheckIn> = emptyList(),
) = Ticket(
    number = number,
    attendeeName = attendeeName,
    attendeeEmail = "magak@example.com",
    type = type,
    event = "DroidconKe & FlutterconKe 2026",
    firstDay = LocalDate(2026, 11, 5),
    lastDay = LocalDate(2026, 11, 6),
    venue = "PrideInn Azure Hotel Nairobi",
    venueAddress = "Westlands",
    status = status,
    checkIns = checkIns,
    qrBase64 = TEST_QR_PNG_BASE64,
)

/** A code-shaped PNG for screenshots; it encodes nothing. */
const val TEST_QR_PNG_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAOgAAADoCAAAAADAwvekAAAB40lEQVR42u3dSRKDIBAAQP//6eQJMIuMJs0tliW0F2bByvX5k3GBgoKCgoKCgoKCgoKCPhp6JcdyosX9q+vV9YCCgoKCgp6Ebm/Eh6DV9YCCgoKCgk5AVxty9P7dQKBrPaCgoKCgoG+ChgvLmzBQUFBQUNB/gK7uCxeeQUFBQUFBXwzNbvjZxnE1IAEFBQUFBX0CNHuA6a7fjzs5BgoKCgoKeuMHPrsb/W4gUV4PKCgoKCjoAWi1wFyFdwUWoKCgoKCgk9D0A29q9EYbzKCgoKCgoBPQ6IKjL+Kuhu9yPlBQUFBQ0AFoNFBYTZwtVO8GKKCgoKCgoJPQ7Eaf3fCzCfxuAAMKCgoKCjoJDSe4yQL2boG6LfEGBQUFBQU9CM1O2NUYjjZ+QUFBQUFBJ6DVhnA1AOhOuEFBQUFBQU9CswlzNYHvejGgoKCgoKCT0K5RLTxnE3VQUFBQUNBJaDUBribu2QZxW2QECgoKCgraAI0GBtFAIfrc6vNBQUFBQUEnoOUGbPEgVbbwDQoKCgoK+kZotUGcLYgv5wUFBQUFBX0xtG3jf3rAAAoKCgoKevX/AU32+i5kWfgGBQUFBQU9CO0+UBVt8GYPVIGCgoKCgk5Cf3WAgoKCgoKCgoKCgoKCPmp8AaNcyCPy2aa5AAAAAElFTkSuQmCC"