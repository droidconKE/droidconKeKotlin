/*
 * Copyright 2022 DroidconKE
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
package com.android254.data.network.models.payloads

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Feedback(val rating: FeedbackRating, val message: String)

@Serializable(with = FeedbackRatingSerializer::class)
enum class FeedbackRating { BAD, OKAY, GOOD }

private class FeedbackRatingSerializer : KSerializer<FeedbackRating> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FeedbackRating", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: FeedbackRating) {
        val code = when (value) {
            FeedbackRating.BAD -> 1
            FeedbackRating.OKAY -> 2
            FeedbackRating.GOOD -> 3
        }
        encoder.encodeInt(code)
    }

    override fun deserialize(decoder: Decoder) =
        when (val code = decoder.decodeInt()) {
            1 -> FeedbackRating.BAD
            2 -> FeedbackRating.OKAY
            3 -> FeedbackRating.GOOD
            else -> throw IllegalArgumentException("Invalid feedback rating code $code. Only 1, 2 and 3 allowed.")
        }
}