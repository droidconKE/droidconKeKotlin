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
package com.android254.presentation.sessions.models

import com.android254.presentation.common.resultstatus.ResultStatus
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionsFilterOption

data class SessionsUiState(
    val sessions: List<SessionPresentationModel> = emptyList(),
    val eventDays: List<EventDate> = emptyList(),
    val sessionStatus: ResultStatus = ResultStatus.Loading,
    /**
     * Filter options derived from the loaded sessions, so an option can never name a
     * room, level or format that no session has. Previously hardcoded in the filter
     * panel, which is how the room filter came to offer "Room A" at a venue whose rooms
     * are named "Opal" and "Sapphire".
     */
    val availableFilters: List<SessionsFilterOption> = emptyList(),
    /**
     * Derived from the filter state rather than mirrored in the composable. The screen
     * used to keep its own non-saveable copy, so rotating the device reset the toggle
     * while leaving the underlying filter applied.
     */
    val showMySessionsOnly: Boolean = false,
    val isFilterActive: Boolean = false,
)