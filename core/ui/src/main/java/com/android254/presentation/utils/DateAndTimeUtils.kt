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

import android.icu.text.DateIntervalFormat
import android.icu.util.DateInterval
import android.text.format.DateUtils
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import timber.log.Timber
import java.text.FieldPosition
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.icu.util.TimeZone as IcuTimeZone
import java.util.Locale as JavaLocale

private val FEED_TIMESTAMP_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

fun String.feedPostedAtMillis(): Long? =
    try {
        LocalDateTime
            .parse(this, FEED_TIMESTAMP_FORMAT)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    } catch (e: DateTimeParseException) {
        Timber.w(e, "Unparseable feed timestamp: %s", this)
        null
    }

fun String.getTimeDifference(nowMillis: Long = System.currentTimeMillis()): String = feedPostedAtMillis()?.let { postedAt -> relativeDay(postedAt, nowMillis) } ?: this

fun relativeDay(
    atMillis: Long,
    nowMillis: Long,
): String = DateUtils.getRelativeTimeSpanString(atMillis, nowMillis, DateUtils.DAY_IN_MILLIS).toString()

fun eventDatesLabel(
    first: LocalDate,
    last: LocalDate,
    locale: JavaLocale,
): String {
    val format = DateIntervalFormat.getInstance("dMMMM", locale).apply { timeZone = IcuTimeZone.GMT_ZONE }
    val interval =
        DateInterval(
            first.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
            last.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        )
    return format.format(interval, StringBuffer(), FieldPosition(0)).toString()
}