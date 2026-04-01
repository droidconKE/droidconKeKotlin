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

import com.android254.presentation.common.results_status.ResultStatus
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionPresentationModel

data class SessionsUiState(
    val sessions: List<SessionPresentationModel> = emptyList(),
    val eventDays: List<EventDate> = emptyList(),
    val selectedEventDay: EventDate = EventDate("-1", 1),
    val sessionStatus: ResultStatus = ResultStatus.Loading,
)