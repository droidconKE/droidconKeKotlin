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
package ke.droidcon.kotlin.datasource.remote.tickets.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssuedTicketsResponse(
    val data: List<IssuedTicketDTO>,
)

@Serializable
data class IssuedTicketDTO(
    @SerialName("ticket_no") val ticketNo: String,
    @SerialName("attendee_name") val attendeeName: String = "",
    @SerialName("attendee_email") val attendeeEmail: String = "",
    val ticket: TicketTypeDTO? = null,
    val event: TicketEventDTO? = null,
    @SerialName("order_status") val orderStatus: String? = null,
    @SerialName("check_ins") val checkIns: List<CheckInDTO> = emptyList(),
    @SerialName("qr_base64") val qrBase64: String = "",
)

@Serializable
data class TicketTypeDTO(
    val title: String = "",
)

@Serializable
data class TicketEventDTO(
    val title: String = "",
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("venue_name") val venueName: String? = null,
    @SerialName("venue_address") val venueAddress: String? = null,
)

@Serializable
data class CheckInDTO(
    val day: String,
    val at: String,
)