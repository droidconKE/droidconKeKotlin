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
package ke.droidcon.kotlin.datasource.remote.tickets

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.mockk
import ke.droidcon.kotlin.datasource.remote.tickets.model.CheckInDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import ke.droidcon.kotlin.datasource.remote.utils.HttpClientFactory
import ke.droidcon.kotlin.datasource.remote.utils.MockTokenProvider
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import ke.droidcon.kotlin.datasource.remote.utils.provideEventBaseUrl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketsApiTest {
    private val toggle = RemoteFeatureToggle(mockk(relaxed = true))

    @Test
    fun `looks a ticket up by its number on this event`() =
        runTest {
            val engine = MockEngine { respondJson("""{"data": []}""") }

            TicketsApi(HttpClientFactory(MockTokenProvider(), toggle).create(engine)).findByNumber("TIC-70235")

            assertEquals(
                "${provideEventBaseUrl()}/tickets?ticket_no=TIC-70235",
                engine.requestHistory
                    .single()
                    .url
                    .toString(),
            )
        }

    @Test
    fun `reads the ticket the backend documents`() =
        runTest {
            val engine = MockEngine { respondJson(DOCUMENTED_TICKET) }

            val result = TicketsApi(HttpClientFactory(MockTokenProvider(), toggle).create(engine)).findByNumber("TIC-679069")

            val ticket = (result as DataResult.Success).data.single()
            assertEquals("TIC-679069", ticket.ticketNo)
            assertEquals("Early bird", ticket.ticket?.title)
            assertEquals("Sarit Expo Centre", ticket.event?.venueName)
            assertEquals("paid", ticket.orderStatus)
            assertEquals(listOf(CheckInDTO("2026-11-05", "2026-11-05T08:41:12+03:00")), ticket.checkIns)
            assertEquals("iVBORw0KGgo=", ticket.qrBase64)
        }

    @Test
    fun `too many requests is told apart from other failures`() =
        runTest {
            val engine = MockEngine { respond("""{"message": "Too Many Attempts."}""", HttpStatusCode.TooManyRequests, jsonHeaders) }

            val result = TicketsApi(HttpClientFactory(MockTokenProvider(), toggle).create(engine)).findByNumber("TIC-1")

            assertTrue((result as DataResult.Error).rateLimited)
        }

    private fun io.ktor.client.engine.mock.MockRequestHandleScope.respondJson(body: String) = respond(body, HttpStatusCode.OK, jsonHeaders)

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private companion object {
        const val DOCUMENTED_TICKET = """
            {
              "data": [
                {
                  "ticket_no": "TIC-679069",
                  "attendee_name": "Ada Lovelace",
                  "attendee_email": "ada@example.com",
                  "ticket": {"id": 14, "title": "Early bird", "price": 1500, "description": "Until the end of September"},
                  "event": {
                    "title": "droidconKE 2026",
                    "slug": "droidconke-2026",
                    "start_date": "2026-11-05",
                    "end_date": "2026-11-07",
                    "venue_name": "Sarit Expo Centre",
                    "venue_address": "Westlands, Nairobi"
                  },
                  "order_no": "ORD-ZJ7OSLCO",
                  "order_status": "paid",
                  "checked_in": true,
                  "check_ins": [{"day": "2026-11-05", "at": "2026-11-05T08:41:12+03:00"}],
                  "qr": "data:image/png;base64,iVBORw0KGgo=",
                  "qr_base64": "iVBORw0KGgo=",
                  "qr_url": "https://api.example.test/v1/events/droidconke-2026/tickets/TIC-679069/qr.png?expires=1&signature=9f2c"
                }
              ]
            }
        """
    }
}