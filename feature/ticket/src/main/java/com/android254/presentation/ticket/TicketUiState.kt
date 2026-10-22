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

import androidx.compose.runtime.Immutable
import com.android254.domain.models.Ticket
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

sealed interface TicketUiState {
    data object Loading : TicketUiState

    data class Ready(
        val ticket: TicketUi?,
        val form: AddTicketForm,
    ) : TicketUiState
}

@Immutable
data class TicketUi(
    val number: String,
    val attendeeName: String,
    val type: String,
    val event: String,
    val firstDay: LocalDate?,
    val lastDay: LocalDate?,
    val venue: String,
    val status: Ticket.Status,
    val checkedInToday: Boolean,
    val qrBase64: String,
)

data class AddTicketForm(
    val number: String = "",
    val email: String = "",
    val isAdding: Boolean = false,
    val error: AddTicketError? = null,
) {
    val canSubmit: Boolean get() = number.isNotBlank() && email.contains('@') && !isAdding
}

enum class AddTicketError { NoMatch, NotATicket, Offline, Busy, Failed, ScannerInstalling, ScannerUnavailable }

sealed interface TicketAction {
    data class NumberChanged(
        val value: String,
    ) : TicketAction

    data class EmailChanged(
        val value: String,
    ) : TicketAction

    data object Add : TicketAction

    data object Scan : TicketAction

    data object Remove : TicketAction
}

fun Ticket.toUi(
    now: Instant,
    zone: TimeZone,
) = TicketUi(
    number = number,
    attendeeName = attendeeName,
    type = type,
    event = event,
    firstDay = firstDay,
    lastDay = lastDay,
    venue = venue,
    status = status,
    checkedInToday = checkIns.any { it.day == now.toLocalDateTime(zone).date },
    qrBase64 = qrBase64,
)