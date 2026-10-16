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
package ke.droidcon.kotlin.core.notifications.push

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * The contract for the `payload` field of an FCM data message.
 *
 * The payload is a JSON object whose `type` names one of the subtypes below. Fields the app does
 * not know yet are ignored, so the backend can add them without breaking installed versions; a
 * `type` the app does not know decodes to null and the message is dropped.
 */
@Serializable
sealed interface PushPayload {
    @Serializable
    @SerialName("schedule_change")
    data class ScheduleChange(
        val sessionId: String,
        val changeType: ChangeType,
        val message: String,
    ) : PushPayload {
        enum class ChangeType { RoomChanged, TimeChanged, Cancelled, SpeakerChanged }
    }

    @Serializable
    @SerialName("announcement")
    data class Announcement(
        val title: String,
        val body: String,
        val deepLink: String? = null,
    ) : PushPayload

    @Serializable
    @SerialName("sync_request")
    data object SyncRequest : PushPayload

    @Serializable
    @SerialName("feed_post")
    data class NewFeedPost(
        val postId: String,
        val preview: String,
    ) : PushPayload

    @Serializable
    @SerialName("milestone")
    data class Milestone(
        val title: String,
        val body: String,
        val icon: String? = null,
    ) : PushPayload

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Decodes [payload], or returns null when it is malformed or of a type this version does not know. */
        fun decodeOrNull(payload: String): PushPayload? =
            try {
                json.decodeFromString(serializer(), payload)
            } catch (e: SerializationException) {
                null
            } catch (e: IllegalArgumentException) {
                null
            }

        fun encode(payload: PushPayload): String = json.encodeToString(serializer(), payload)
    }
}