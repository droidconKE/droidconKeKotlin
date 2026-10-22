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
package com.android254.domain.tickets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TicketCodeTest {
    @Test
    fun `a printed ticket's code gives its number`() {
        assertEquals("TIC-70235", ticketNumberIn("https://events.droidcon.co.ke/checkin/droidconke-2026-731/attendee/TIC-70235"))
    }

    @Test
    fun `a renamed event's older address still gives the number`() {
        assertEquals("TIC-70235", ticketNumberIn("https://events.droidcon.co.ke/checkin/droidconke-2026/attendee/tic-70235?ref=pdf"))
    }

    @Test
    fun `a bare number is a ticket, anything else is not`() {
        assertEquals("TIC-70235", ticketNumberIn(" tic-70235 "))
        assertNull(ticketNumberIn("https://droidcon.co.ke"))
        assertNull(ticketNumberIn("WIFI:S:droidcon;T:WPA;P:secret;;"))
    }

    @Test
    fun `a code from somewhere else, or a token that is not a ticket number, is not a ticket`() {
        assertNull(ticketNumberIn("https://anywhere.example/checkin/x/attendee/not-a-ticket"))
        assertNull(ticketNumberIn("https://events.droidcon.co.ke/checkin/x/attendee/"))
    }

    @Test
    fun `a typed number ignores spaces and case, and a pasted link gives its number`() {
        assertEquals("TIC-70235", typedTicketNumber(" tic - 70235"))
        assertEquals("TIC-70235", typedTicketNumber("https://events.droidcon.co.ke/checkin/droidconke-2026-731/attendee/TIC-70235"))
        assertNull(typedTicketNumber("my ticket"))
    }
}