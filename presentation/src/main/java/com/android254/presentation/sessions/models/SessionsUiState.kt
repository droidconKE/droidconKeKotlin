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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

data class SessionsUiState(
    val sessions: ImmutableMap<String, ImmutableList<SessionPresentationModel>> = persistentMapOf(),
    val eventDays: ImmutableList<EventDate> = persistentListOf(),
    val sessionStatus: ResultStatus = ResultStatus.Loading,
    /** Derived from the loaded sessions, so no option can name a value no session has. */
    val availableFilters: ImmutableList<SessionsFilterOption> = persistentListOf(),
    val showMySessionsOnly: Boolean = false,
    val isFilterActive: Boolean = false,
)