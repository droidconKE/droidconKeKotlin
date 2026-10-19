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
package com.android254.presentation.feed.view

import com.android254.domain.models.ScheduleUpdate
import com.android254.presentation.models.FeedUI

/** One row of Updates: news from the organisers, or a change a sync found in the schedule. */
sealed interface UpdateItem {
    val key: String
    val atMillis: Long?

    data class Post(
        val feed: FeedUI,
        override val atMillis: Long?,
    ) : UpdateItem {
        override val key: String = "post:${feed.title}"
    }

    data class Schedule(
        val kind: ScheduleUpdate.Kind,
        val sessionId: String?,
        val title: String,
        val count: Int,
        val whenLabel: String,
        val sessionTime: String?,
        val venue: String,
        val isStarred: Boolean,
        override val atMillis: Long?,
    ) : UpdateItem {
        override val key: String = "update:$kind:$sessionId:$atMillis"
    }
}