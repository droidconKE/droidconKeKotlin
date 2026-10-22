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
package com.android254.domain.models

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class Ticket(
    val number: String,
    val attendeeName: String,
    val attendeeEmail: String,
    val type: String,
    val event: String,
    val firstDay: LocalDate?,
    val lastDay: LocalDate?,
    val venue: String,
    val venueAddress: String,
    val status: Status,
    val checkIns: List<CheckIn>,
    val qrBase64: String,
) {
    /** An unknown status shows its code and leaves it to the gate. */
    enum class Status {
        Active,
        Refunded,
        Revoked,
        Unknown,
        ;

        val showsCode: Boolean get() = this == Active || this == Unknown
    }

    data class CheckIn(
        val day: LocalDate,
        val at: Instant,
    )
}