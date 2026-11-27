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
package com.android254.presentation.ask

import com.android254.domain.models.Session
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** The instructions that make Gemini answer in A2UI, grounded on the sessions in the app. */
internal object AskPrompt {
    const val SURFACE_ID = "answer"

    fun deleteSurface(surfaceId: String): String =
        buildJsonObject {
            put("version", PROTOCOL_VERSION)
            putJsonObject("deleteSurface") { put("surfaceId", surfaceId) }
        }.toString()

    fun system(catalogSchema: String): String =
        """
        You answer questions about the droidcon Kenya conference schedule inside its Android app.
        Use only the sessions in the schedule you are given. Never invent a session, time, room or speaker.

        Reply with a JSON array of A2UI $PROTOCOL_VERSION messages and nothing else:
        1. {"version": "$PROTOCOL_VERSION", "createSurface": {"surfaceId": "$SURFACE_ID", "catalogId": "${AskCatalog.ID}"}}
        2. {"version": "$PROTOCOL_VERSION", "updateComponents": {"surfaceId": "$SURFACE_ID", "components": [...]}}

        Components are a flat list. Each has a unique "id" and a "component" type, plus that type's properties
        directly on the object. Exactly one component has the id "root"; make it a Column whose "children" lists
        the other ids in order. Start with one short Text that answers the question in a sentence, then one
        SessionCard per matching session, at most five. Copy "sessionId" exactly from the schedule.
        Set "starred" to true for sessions the schedule marks as starred. Write times as HH:mm, such as 16:00.
        If nothing matches, return only the Column and a Text saying so.

        The components you may use are defined by this JSON Schema catalog:
        $catalogSchema
        """.trimIndent()

    fun user(
        question: String,
        schedule: String,
    ): String = "Schedule, one session per line:\n$schedule\n\nQuestion: $question"

    fun schedule(sessions: List<Session>): String =
        sessions
            .filterNot { it.isServiceSession }
            .joinToString("\n") { session ->
                listOf(
                    "sessionId=${session.id}",
                    "day=${session.eventDay}",
                    "time=${session.startTime}-${session.endTime}",
                    "room=${session.roomList.joinToString()}",
                    "title=${session.title}",
                    "speakers=${session.speakers.joinToString { it.name }}",
                    "level=${session.sessionLevel}",
                    "format=${session.sessionFormat}",
                    "starred=${session.isBookmarked}",
                ).joinToString(" | ")
            }
}