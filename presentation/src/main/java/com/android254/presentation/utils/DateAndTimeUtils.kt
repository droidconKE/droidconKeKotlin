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
package com.android254.presentation.utils

import android.text.format.DateUtils
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val FEED_TIMESTAMP_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

/**
 * Renders an ISO-like timestamp as a relative span, e.g. "2 days ago".
 *
 * Two problems with the previous implementation:
 *
 *  - `SimpleDateFormat.parse` returns a nullable `Date`, and the result was dereferenced
 *    without a null check. The NPE was then swallowed by a broad `catch (e: Exception)`,
 *    so a malformed timestamp silently rendered as the raw ISO string in the feed.
 *  - `SimpleDateFormat` is not thread-safe and was constructed per call.
 *
 * Now uses `java.time` (available on minSdk 24 via core library desugaring) with an
 * immutable, shared formatter, and catches only parse failures. Unparseable input still
 * falls back to the original string, but that is now a deliberate branch rather than the
 * side effect of hiding a crash.
 *
 * @param nowMillis current time, injectable so the relative output is testable.
 */
fun String.getTimeDifference(nowMillis: Long = System.currentTimeMillis()): String =
    try {
        val postedAt =
            LocalDateTime.parse(this, FEED_TIMESTAMP_FORMAT)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        DateUtils.getRelativeTimeSpanString(
            postedAt,
            nowMillis,
            DateUtils.DAY_IN_MILLIS,
        ).toString()
    } catch (e: DateTimeParseException) {
        Timber.w(e, "Unparseable feed timestamp: %s", this)
        this
    }