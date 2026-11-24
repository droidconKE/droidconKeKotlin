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
package com.android254.presentation.common.calendar

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.runtime.Immutable
import com.android254.presentation.common.navigation.DeepLinks
import ke.droidcon.kotlin.core.common.time.VenueZone
import ke.droidcon.kotlin.core.ui.R
import kotlinx.collections.immutable.ImmutableList
import timber.log.Timber

@Immutable
data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String,
    val venue: String,
    val speakers: ImmutableList<String>,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
)

/**
 * Builds a [CalendarContract] insert intent for a single conference session.
 * Reminder timing is intentionally left to the user's calendar app defaults because
 * `ACTION_INSERT` ignores `CalendarContract.Reminders` extras without `WRITE_CALENDAR`.
 */
fun addSessionToCalendarIntent(
    event: CalendarEvent,
    speakersLine: String? = null,
): Intent =
    Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, event.title)
        if (event.venue.isNotBlank()) {
            putExtra(CalendarContract.Events.EVENT_LOCATION, event.venue)
        }
        putExtra(CalendarContract.Events.DESCRIPTION, buildEventDescription(event, speakersLine))
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startEpochMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endEpochMillis)
        putExtra(CalendarContract.Events.EVENT_TIMEZONE, VenueZone.id)
    }

/**
 * Launches the device calendar app for [event], returning `false` if no calendar handler is
 * installed. Adds [Intent.FLAG_ACTIVITY_NEW_TASK] when called from a non-[Activity] context.
 */
fun launchAddToCalendar(
    context: Context,
    event: CalendarEvent,
): Boolean {
    val speakersLine =
        event.speakers
            .takeIf { it.isNotEmpty() }
            ?.let { context.getString(R.string.calendar_event_speakers, it.joinToString(", ")) }
    val intent =
        addSessionToCalendarIntent(event = event, speakersLine = speakersLine).apply {
            if (context.findActivity() == null) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No calendar app available to insert session %s", event.id)
        false
    }
}

internal fun buildEventDescription(
    event: CalendarEvent,
    speakersLine: String? = null,
): String =
    buildList {
        val summary = event.description.trim().truncateSafely(MAX_DESCRIPTION_LENGTH)
        if (summary.isNotEmpty()) add(summary)
        val formattedSpeakers = speakersLine?.trim().orEmpty()
        if (formattedSpeakers.isNotEmpty()) add(formattedSpeakers)
        val cleanId = event.id.trim()
        if (cleanId.isNotEmpty()) add(DeepLinks.session(cleanId))
    }.joinToString("\n\n")

/** Truncates without splitting a UTF-16 surrogate pair and appends an ellipsis when shortened. */
private fun String.truncateSafely(maxChars: Int): String {
    if (length <= maxChars) return this
    var end = maxChars
    if (end > 0 && Character.isHighSurrogate(this[end - 1])) {
        end -= 1
    }
    return substring(0, end).trimEnd() + "…"
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private const val MAX_DESCRIPTION_LENGTH = 500