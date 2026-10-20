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
package com.android254.data.repos

import com.android254.data.repos.mappers.headline
import com.android254.domain.models.ScheduleUpdate.Kind
import ke.droidcon.kotlin.datasource.local.model.SessionEntity

internal data class ScheduleChange(
    val kind: Kind,
    val sessionId: String? = null,
    val title: String = "",
    val count: Int = 0,
)

/**
 * What a sync changed. A first sync, or one with no session in common with the last (a new
 * event year), is one "schedule is out"; breaks coming and going are not news, moving them is.
 */
internal fun scheduleChanges(
    previous: List<SessionEntity>,
    incoming: List<SessionEntity>,
): List<ScheduleChange> {
    if (incoming.isEmpty()) return emptyList()
    val before = previous.associateBy { it.remote_id }
    if (incoming.none { it.remote_id in before }) return listOf(ScheduleChange(Kind.ScheduleOut, count = incoming.size))

    val after = incoming.associateBy { it.remote_id }
    val changes =
        buildList {
            incoming
                .filter { it.remote_id !in before && !it.isServiceSession }
                .forEach { add(ScheduleChange(Kind.Added, it.remote_id, it.headline)) }
            previous
                .filter { it.remote_id !in after && !it.isServiceSession }
                .forEach { add(ScheduleChange(Kind.Removed, title = it.headline)) }
            incoming.forEach { new ->
                val old = before[new.remote_id] ?: return@forEach
                if (old.startTimestamp != new.startTimestamp || old.endTimeStamp != new.endTimeStamp) {
                    add(ScheduleChange(Kind.Rescheduled, new.remote_id, new.headline))
                }
                if (old.roomSet != new.roomSet) add(ScheduleChange(Kind.Moved, new.remote_id, new.headline))
            }
        }
    return if (changes.size > MAX_SEPARATE_CHANGES) listOf(ScheduleChange(Kind.ManyChanges, count = changes.size)) else changes
}

private val SessionEntity.roomSet: Set<String>
    get() =
        rooms
            .split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
            .toSet()

private const val MAX_SEPARATE_CHANGES = 12