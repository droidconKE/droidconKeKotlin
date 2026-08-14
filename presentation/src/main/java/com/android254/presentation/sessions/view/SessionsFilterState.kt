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
package com.android254.presentation.sessions.view

import com.android254.domain.models.Session
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.utils.SessionsFilterCategory

/**
 * The active session filters. Comparisons are case-insensitive, since option values and
 * session values both originate from the API and its casing is not guaranteed.
 */
data class SessionsFilterState(
    val levels: List<String> = emptyList(),
    val rooms: List<String> = emptyList(),
    val sessionTypes: List<String> = emptyList(),
    val isBookmarked: Boolean = false,
) {
    val isActive: Boolean
        get() = levels.isNotEmpty() || rooms.isNotEmpty() || sessionTypes.isNotEmpty() || isBookmarked

    /** A session matches when it satisfies every non-empty facet. */
    fun matches(session: Session): Boolean =
        levels.matchesOrEmpty(session.sessionLevel) &&
            sessionTypes.matchesOrEmpty(session.sessionFormat) &&
            rooms.matchesAnyOrEmpty(session.roomList) &&
            (!isBookmarked || session.isBookmarked)

    /** Returns this state with [option] toggled on or off. */
    fun toggle(option: SessionsFilterOption): SessionsFilterState =
        when (option.type) {
            SessionsFilterCategory.Level -> copy(levels = levels.toggle(option.value))
            SessionsFilterCategory.Room -> copy(rooms = rooms.toggle(option.value))
            SessionsFilterCategory.SessionType -> copy(sessionTypes = sessionTypes.toggle(option.value))
        }

    private fun List<String>.toggle(value: String): List<String> =
        if (any { it.equals(value, ignoreCase = true) }) {
            filterNot { it.equals(value, ignoreCase = true) }
        } else {
            this + value
        }

    private fun List<String>.matchesOrEmpty(value: String): Boolean = isEmpty() || any { it.equals(value.trim(), ignoreCase = true) }

    private fun List<String>.matchesAnyOrEmpty(values: List<String>): Boolean = isEmpty() || values.any { value -> any { it.equals(value, ignoreCase = true) } }

    companion object {
        /** Convenience for tests: a state with only [option] applied. */
        fun from(option: SessionsFilterOption): SessionsFilterState = SessionsFilterState().toggle(option)
    }
}