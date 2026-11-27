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

import androidx.a2ui.model.protocol.A2uiEventAction
import ke.droidcon.kotlin.core.testing.testSession
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AskTest {
    @Test
    fun `a fenced array of messages splits into one message each`() {
        val reply =
            """
            ```json
            [{"version": "v0.9.1", "createSurface": {"surfaceId": "answer", "catalogId": "c"}},
             {"version": "v0.9.1", "updateComponents": {"surfaceId": "answer", "components": []}}]
            ```
            """.trimIndent()

        val messages = splitA2uiMessages(reply)

        assertEquals(2, messages.size)
        assertTrue(messages[0].contains("createSurface"))
        assertTrue(messages[1].contains("updateComponents"))
    }

    @Test
    fun `a message without a version gets the protocol version the renderer accepts`() {
        val messages = splitA2uiMessages("""{"messages": [{"deleteSurface": {"surfaceId": "answer"}}]}""")

        val version =
            Json
                .parseToJsonElement(messages.single())
                .jsonObject["version"]
                ?.jsonPrimitive
                ?.content
        assertEquals("v0.9.1", version)
    }

    @Test
    fun `a JSON object that is not an A2UI message is dropped`() {
        assertTrue(splitA2uiMessages("""{"answer": "There is one Compose session."}""").isEmpty())
    }

    @Test
    fun `deleting a surface names that surface`() {
        val message = Json.parseToJsonElement(AskPrompt.deleteSurface("answer")).jsonObject

        assertEquals(
            "answer",
            message["deleteSurface"]
                ?.jsonObject
                ?.get("surfaceId")
                ?.jsonPrimitive
                ?.content,
        )
        assertEquals("v0.9.1", message["version"]?.jsonPrimitive?.content)
    }

    @Test
    fun `an empty reply yields no messages`() {
        assertTrue(splitA2uiMessages("  ").isEmpty())
    }

    @Test
    fun `the star event carries the session id to bookmark`() {
        val action = A2uiEventAction("answer", "card_1", 0L, SessionCardComponent.STAR_EVENT, mapOf("sessionId" to "42"))

        assertEquals("42", action.starredSessionId())
    }

    @Test
    fun `any other event is not a star`() {
        val action = A2uiEventAction("answer", "card_1", 0L, "openDetails", mapOf("sessionId" to "42"))

        assertNull(action.starredSessionId())
    }

    @Test
    fun `the schedule sent to the model keeps ids and starred state and drops service sessions`() {
        val schedule =
            AskPrompt.schedule(
                listOf(
                    testSession(id = "7", starred = true, title = "Compose after lunch"),
                    testSession(id = "8", title = "Lunch").copy(isServiceSession = true),
                ),
            )

        assertTrue(schedule.contains("sessionId=7"))
        assertTrue(schedule.contains("starred=true"))
        assertFalse(schedule.contains("Lunch |"))
        assertFalse(schedule.contains("sessionId=8"))
    }
}