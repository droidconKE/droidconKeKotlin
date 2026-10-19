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
package com.android254.presentation.common.navigation

import java.net.URI
import java.net.URISyntaxException

/**
 * The `droidconke://` links that notifications open, and the destinations they resolve to.
 *
 * The scheme and hosts here must match the `<intent-filter>` on `MainActivity`; a link that
 * resolves here but not there is never delivered to the app.
 */
object DeepLinks {
    const val SCHEME = "droidconke"
    private const val HOST_SESSION = "session"
    private const val HOST_FEED = "feed"

    fun session(sessionId: String): String = "$SCHEME://$HOST_SESSION/$sessionId"

    fun feed(): String = "$SCHEME://$HOST_FEED"

    /** The destination [uri] opens, or null when it is not one of the app's links. */
    fun parse(uri: String): Screens? {
        val parsed =
            try {
                URI(uri)
            } catch (e: URISyntaxException) {
                return null
            }
        if (parsed.scheme != SCHEME) return null
        val segments =
            parsed.path
                .orEmpty()
                .split('/')
                .filter { it.isNotEmpty() }
        return when (parsed.host) {
            HOST_SESSION -> segments.firstOrNull()?.let { Screens.SessionDetails(it) }
            HOST_FEED -> Screens.Feed
            else -> null
        }
    }
}