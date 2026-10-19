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
package com.android254.presentation.feed.view

import android.text.format.DateFormat
import com.android254.domain.models.Feed
import com.android254.domain.models.ScheduleUpdate
import com.android254.domain.models.Session
import com.android254.presentation.models.FeedUI
import com.android254.presentation.utils.feedPostedAtMillis
import com.android254.presentation.utils.getTimeDifference
import com.android254.presentation.utils.relativeDay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant

fun Feed.toPresentation() =
    FeedUI(
        title = title,
        body = body,
        topic = topic,
        url = url,
        image = image,
        createdAt = createdAt.getTimeDifference(),
    )

/** Organiser posts and schedule changes on one timeline, newest first; a post with no readable time sinks. */
internal fun updateItems(
    posts: List<Feed>,
    updates: List<ScheduleUpdate>,
    sessions: List<Session>,
    now: Instant,
    zone: TimeZone,
): List<UpdateItem> {
    val byId = sessions.associateBy { it.remoteId }
    val nowMillis = now.toEpochMilliseconds()
    val postItems = posts.map { UpdateItem.Post(it.toPresentation(), it.createdAt.feedPostedAtMillis()) }
    val scheduleItems =
        updates.map { update ->
            val session = update.sessionId?.let(byId::get)
            UpdateItem.Schedule(
                kind = update.kind,
                sessionId = session?.remoteId,
                title = session?.title ?: update.title,
                count = update.count,
                whenLabel = relativeDay(update.at.toEpochMilliseconds(), nowMillis),
                sessionTime = session?.startInstant?.whenLabel(zone),
                venue = session?.roomList?.joinToString(", ").orEmpty(),
                isStarred = session?.isBookmarked == true,
                atMillis = update.at.toEpochMilliseconds(),
            )
        }
    return (postItems + scheduleItems).sortedByDescending { it.atMillis ?: Long.MIN_VALUE }
}

/** "Thu, 5 Nov · 10:15 AM" in the venue's time, the way the agenda writes a session's start. */
private fun Instant.whenLabel(zone: TimeZone): String {
    val locale = Locale.getDefault()
    val local = toLocalDateTime(zone)
    val day = local.date.toJavaLocalDate().format(DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "EEEdMMM"), locale))
    val time = local.time.toJavaLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a", locale))
    return "$day · $time"
}