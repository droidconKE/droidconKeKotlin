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
package com.android254.data.repos.mappers

import com.android254.domain.models.Ticket
import ke.droidcon.kotlin.datasource.local.model.TicketEntity
import ke.droidcon.kotlin.datasource.remote.tickets.model.IssuedTicketDTO
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import kotlin.time.Instant

fun IssuedTicketDTO.toEntity() =
    TicketEntity(
        number = ticketNo,
        attendeeName = attendeeName,
        attendeeEmail = attendeeEmail,
        type = ticket?.title.orEmpty(),
        event = event?.title.orEmpty(),
        firstDay = event?.startDate?.datePart(),
        lastDay = event?.endDate?.datePart(),
        venue = event?.venueName.orEmpty(),
        venueAddress = event?.venueAddress.orEmpty(),
        status = orderStatus.orEmpty(),
        checkIns = Json.encodeToString(checkIns.map { StoredCheckIn(it.day, it.at) }),
        qrBase64 = qrBase64,
    )

fun TicketEntity.toDomain() =
    Ticket(
        number = number,
        attendeeName = attendeeName,
        attendeeEmail = attendeeEmail,
        type = type,
        event = event,
        firstDay = firstDay?.let(::dateOrNull),
        lastDay = lastDay?.let(::dateOrNull),
        venue = venue,
        venueAddress = venueAddress,
        status = status.toTicketStatus(),
        checkIns =
            runCatching { Json.decodeFromString<List<StoredCheckIn>>(checkIns) }
                .getOrDefault(emptyList())
                .mapNotNull { checkIn ->
                    val day = dateOrNull(checkIn.day) ?: return@mapNotNull null
                    val at = runCatching { Instant.parse(checkIn.at) }.getOrNull() ?: return@mapNotNull null
                    Ticket.CheckIn(day, at)
                },
        qrBase64 = qrBase64,
    )

/** A ticket with no order, such as a sponsor's guest, has no status and is valid. */
private fun String.toTicketStatus(): Ticket.Status =
    when (lowercase()) {
        "", "active", "paid" -> Ticket.Status.Active
        "refunded" -> Ticket.Status.Refunded
        "revoked" -> Ticket.Status.Revoked
        else -> Ticket.Status.Unknown.also { Timber.w("Unknown ticket status %s", this) }
    }

/** Not the DTO, so an API change cannot empty saved check-ins. */
@Serializable
private data class StoredCheckIn(
    val day: String,
    val at: String,
)

/** Event dates come as a date or as midnight with an offset. */
private fun String.datePart(): String? = take(ISO_DATE_LENGTH).takeIf { dateOrNull(it) != null }

private fun dateOrNull(text: String): LocalDate? = runCatching { LocalDate.parse(text.take(ISO_DATE_LENGTH)) }.getOrNull()

private const val ISO_DATE_LENGTH = 10