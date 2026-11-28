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

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Splits a model reply into one JSON string per A2UI message, tolerating a code fence or a wrapper object. */
internal fun splitA2uiMessages(reply: String): List<String> {
    val json =
        reply
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    if (json.isEmpty()) return emptyList()
    val messages =
        when (val root = Json.parseToJsonElement(json)) {
            is JsonArray -> root.toList()
            is JsonObject -> (root["messages"] as? JsonArray)?.toList() ?: listOf(root)
            else -> emptyList()
        }
    return messages
        .filterIsInstance<JsonObject>()
        .filter { message -> MESSAGE_KEYS.any(message::containsKey) }
        .map { it.withVersion().toString() }
}

private fun JsonObject.withVersion(): JsonElement = if (containsKey(VERSION_KEY)) this else JsonObject(mapOf(VERSION_KEY to JsonPrimitive(PROTOCOL_VERSION)) + this)

internal const val PROTOCOL_VERSION = "v0.9.1"
private const val VERSION_KEY = "version"
private val MESSAGE_KEYS = listOf("createSurface", "updateComponents", "updateDataModel", "deleteSurface")