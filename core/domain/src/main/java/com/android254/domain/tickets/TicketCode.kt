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

private val checkInPath = Regex("""/checkin/[^/]+/attendee/([^/?#]+)""")
private val ticketShape = Regex("""^TIC-[A-Z0-9]+$""")

/** The number in a ticket's code, the gate's `…/checkin/{event}/attendee/{ticket_no}` address, or a bare number. */
fun ticketNumberIn(code: String): String? {
    val trimmed = code.trim()
    val candidate = checkInPath.find(trimmed)?.groupValues?.get(1) ?: trimmed
    return candidate.uppercase().takeIf(ticketShape::matches)
}

fun typedTicketNumber(typed: String): String? = ticketNumberIn(typed.filterNot(Char::isWhitespace))