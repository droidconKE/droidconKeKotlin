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

import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionPresentationModel

/*sealed interface SessionsUiState {
    object Idle : SessionsUiState
    object Loading : SessionsUiState
    data class Data(val data: List<SessionPresentationModel>) : SessionsUiState

    @JvmInline
    value class Error(val message: String) : SessionsUiState

    @JvmInline
    value class Empty(val message: String) : SessionsUiState
}*/

data class SessionsUiState(
    val isEmpty: Boolean = false,
    val isEmptyMessage: String = "",
    val isLoading: Boolean = true,
    val sessions: List<SessionPresentationModel> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String = "",
    val eventDays: List<EventDate> = emptyList(),
    val selectedEventDay: EventDate = EventDate("1", 1)
)