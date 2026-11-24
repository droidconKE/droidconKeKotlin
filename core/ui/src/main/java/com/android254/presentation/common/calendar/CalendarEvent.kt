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

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.runtime.Immutable
import androidx.core.content.FileProvider
import com.android254.presentation.common.navigation.DeepLinks
import ke.droidcon.kotlin.core.common.time.VenueZone
import kotlinx.collections.immutable.ImmutableList
import timber.log.Timber
import java.io.File
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Instant

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

/** Builds a CalendarContract insert intent for a single conference session. */
fun addSessionToCalendarIntent(event: CalendarEvent): Intent =
    Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, event.title)
        if (event.venue.isNotBlank()) {
            putExtra(CalendarContract.Events.EVENT_LOCATION, event.venue)
        }
        putExtra(CalendarContract.Events.DESCRIPTION, buildEventDescription(event))
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startEpochMillis)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endEpochMillis)
        putExtra(CalendarContract.Events.EVENT_TIMEZONE, VenueZone.id)
        putExtra(CalendarContract.Reminders.MINUTES, REMINDER_MINUTES)
    }

/** Launches the device calendar app for [event], returning false if no calendar handler is installed. */
fun launchAddToCalendar(
    context: Context,
    event: CalendarEvent,
): Boolean =
    try {
        context.startActivity(addSessionToCalendarIntent(event))
        true
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No calendar app available to insert session %s", event.id)
        false
    }

/**
 * Emits an RFC 5545 `.ics` calendar containing every valid session in [events].
 * Stable UIDs let re-imports update existing entries instead of creating duplicates.
 */
fun buildIcs(
    events: List<CalendarEvent>,
    generatedAt: Instant = Clock.System.now(),
): String {
    val dtStamp = formatIcsUtc(generatedAt.toEpochMilliseconds())
    return buildString {
        appendLineCrlf("BEGIN:VCALENDAR")
        appendLineCrlf("VERSION:2.0")
        appendLineCrlf("PRODID:-//droidcon Kenya//Android App//EN")
        appendLineCrlf("CALSCALE:GREGORIAN")
        appendLineCrlf("X-WR-CALNAME:droidcon Kenya — My Agenda")
        appendLineCrlf("X-WR-TIMEZONE:${VenueZone.id}")

        events
            .filter { it.endEpochMillis > it.startEpochMillis }
            .forEach { event ->
                appendLineCrlf("BEGIN:VEVENT")
                appendLineCrlf("UID:${event.id}@droidcon.co.ke")
                appendLineCrlf("DTSTAMP:$dtStamp")
                appendLineCrlf("DTSTART:${formatIcsUtc(event.startEpochMillis)}")
                appendLineCrlf("DTEND:${formatIcsUtc(event.endEpochMillis)}")
                appendLineCrlf("SUMMARY:${event.title.escapeIcs()}")
                if (event.venue.isNotBlank()) {
                    appendLineCrlf("LOCATION:${event.venue.escapeIcs()}")
                }
                appendLineCrlf("DESCRIPTION:${buildEventDescription(event).escapeIcs()}")
                appendLineCrlf("BEGIN:VALARM")
                appendLineCrlf("TRIGGER:-PT${REMINDER_MINUTES}M")
                appendLineCrlf("ACTION:DISPLAY")
                appendLineCrlf("DESCRIPTION:${event.title.escapeIcs()} starts in $REMINDER_MINUTES minutes")
                appendLineCrlf("END:VALARM")
                appendLineCrlf("END:VEVENT")
            }

        appendLineCrlf("END:VCALENDAR")
    }
}

/** Writes [events] to a cached `.ics` file and opens a share sheet backed by [FileProvider]. */
fun shareAgendaIcs(
    context: Context,
    events: List<CalendarEvent>,
    chooserTitle: String,
    generatedAt: Instant = Clock.System.now(),
): Boolean {
    if (events.isEmpty()) return false
    val dir = File(context.cacheDir, CALENDAR_CACHE_DIR).apply { mkdirs() }
    val file = File(dir, AGENDA_FILE_NAME)
    file.writeText(buildIcs(events, generatedAt), Charsets.UTF_8)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = ICS_MIME_TYPE
            putExtra(Intent.EXTRA_SUBJECT, chooserTitle)
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(AGENDA_FILE_NAME, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    val chooser =
        Intent.createChooser(send, chooserTitle).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    return try {
        context.startActivity(chooser)
        true
    } catch (e: ActivityNotFoundException) {
        Timber.w(e, "No app available to handle exported .ics agenda")
        false
    }
}

private fun buildEventDescription(event: CalendarEvent): String =
    buildList {
        val summary = event.description.trim().take(MAX_DESCRIPTION_LENGTH)
        if (summary.isNotEmpty()) add(summary)
        if (event.speakers.isNotEmpty()) add("Speakers: ${event.speakers.joinToString(", ")}")
        add(DeepLinks.session(event.id))
    }.joinToString("\n\n")

private fun StringBuilder.appendLineCrlf(line: String) {
    append(line)
    append("\r\n")
}

private fun String.escapeIcs(): String =
    replace("\\", "\\\\")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace("\n", "\\n")
        .replace(";", "\\;")
        .replace(",", "\\,")

private fun formatIcsUtc(epochMillis: Long): String =
    ICS_UTC_FORMATTER.format(
        java.time.Instant
            .ofEpochMilli(epochMillis)
            .atOffset(ZoneOffset.UTC),
    )

private val ICS_UTC_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.US)

private const val REMINDER_MINUTES = 10
private const val MAX_DESCRIPTION_LENGTH = 500
private const val CALENDAR_CACHE_DIR = "calendar"
private const val AGENDA_FILE_NAME = "droidconke-agenda.ics"
private const val ICS_MIME_TYPE = "text/calendar"