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
package ke.droidcon.kotlin.core.common.time

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** The venue's time zone. A time the API sends without an offset is in it. */
val VenueZone: ZoneId = ZoneId.of("Africa/Nairobi")

/**
 * Reads an API time in either form it has sent: venue-local `2025-11-05 08:00:00`, or ISO-8601 with
 * an offset, `2025-11-05T08:00:00+03:00`. Throws [DateTimeParseException] for anything else.
 */
fun parseApiDateTime(text: String): ZonedDateTime =
    when (val parsed = ApiDateTime.parseBest(text, OffsetDateTime::from, LocalDateTime::from)) {
        is OffsetDateTime -> parsed.atZoneSameInstant(VenueZone)
        is LocalDateTime -> parsed.atZone(VenueZone)
        else -> throw DateTimeParseException("Unreadable API time", text, 0)
    }

private val ApiDateTime: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd['T'][ ]HH:mm:ss[XXX]")