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
package com.android254.domain.repos

import com.android254.domain.models.Ticket
import kotlinx.coroutines.flow.Flow

interface TicketsRepo {
    fun ticket(): Flow<Ticket?>

    /** The [email] stops a mistyped number landing on somebody else's ticket. */
    suspend fun add(
        number: String,
        email: String,
    ): AddTicketResult

    suspend fun addScanned(code: String): AddTicketResult

    /** A failure keeps what is saved. */
    suspend fun refresh()

    suspend fun remove()
}

sealed interface AddTicketResult {
    data class Added(
        val ticket: Ticket,
    ) : AddTicketResult

    data object NoMatch : AddTicketResult

    data object NotATicket : AddTicketResult

    data object Offline : AddTicketResult

    data object Busy : AddTicketResult

    data object ScannerUnavailable : AddTicketResult

    data object Failed : AddTicketResult
}