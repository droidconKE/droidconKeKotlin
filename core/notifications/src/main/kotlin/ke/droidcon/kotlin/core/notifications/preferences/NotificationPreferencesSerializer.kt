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
package ke.droidcon.kotlin.core.notifications.preferences

import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * JSON serializer for NotificationPreferences in DataStore.
 *
 * Handles reading and writing preference objects to/from JSON format.
 * On read errors, returns the default preference values.
 */
object NotificationPreferencesSerializer : Serializer<NotificationPreferences> {
    override val defaultValue: NotificationPreferences = NotificationPreferences()

    override suspend fun readFrom(input: InputStream): NotificationPreferences {
        return try {
            val json = input.readBytes().decodeToString()
            Json.decodeFromString<NotificationPreferences>(json)
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(
        t: NotificationPreferences,
        output: OutputStream,
    ) {
        val json = Json.encodeToString(NotificationPreferences.serializer(), t)
        output.write(json.encodeToByteArray())
    }
}
