/*
 * Copyright 2023 DroidconKE
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
package ke.droidcon.kotlin.datasource.remote.feed.deserializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The API writes times venue-local with no offset, `2025-11-05 08:00:00`, or ISO-8601 with one,
 * `2025-11-05T08:00:00+03:00`; either reads as the venue's local time.
 */
internal class LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    private val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
    private val reader = DateTimeFormatter.ofPattern("uuuu-MM-dd['T'][ ]HH:mm:ss[XXX]")

    override fun serialize(
        encoder: Encoder,
        value: LocalDateTime,
    ) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val text = decoder.decodeString()
        return when (val parsed = reader.parseBest(text, OffsetDateTime::from, LocalDateTime::from)) {
            is OffsetDateTime -> parsed.atZoneSameInstant(VENUE).toLocalDateTime()
            is LocalDateTime -> parsed
            else -> error("Unreadable feed time: $text")
        }
    }
}

private val VENUE: ZoneId = ZoneId.of("Africa/Nairobi")