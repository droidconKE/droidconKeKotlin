/*
 * Copyright 2022 DroidconKE
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
package com.android254.presentation.sessionDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.repos.SessionsRepo
import com.android254.presentation.common.navigation.Screens
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.sessions.mappers.toSessionDetailsPresentationModal
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SessionDetailsUiState {
    object Loading : SessionDetailsUiState

    data class Success(val data: SessionDetailsPresentationModel) : SessionDetailsUiState

    data class Error(val message: String) : SessionDetailsUiState
}

@HiltViewModel(assistedFactory = SessionDetailsViewModel.Factory::class)
class SessionDetailsViewModel
    @AssistedInject
    constructor(
        @Assisted val navKey: Screens.SessionDetails,
        private val sessionsRepo: SessionsRepo,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(navKey: Screens.SessionDetails): SessionDetailsViewModel
        }

        private val sessionId = navKey.sessionId

        val uiState =
            sessionsRepo.fetchSessionById(id = sessionId)
                .map {
                    if (it == null) {
                        SessionDetailsUiState.Error(message = "Session Info not found")
                    } else {
                        SessionDetailsUiState.Success(it.toSessionDetailsPresentationModal())
                    }
                }
                .onStart { SessionDetailsUiState.Loading }
                .catch { SessionDetailsUiState.Error(message = "An unexpected error occurred") }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = SessionDetailsUiState.Loading,
                )

        fun bookmarkSession(sessionId: String) =
            viewModelScope.launch {
                sessionsRepo.bookmarkSession(sessionId)
            }

        fun unBookmarkSession(sessionId: String) =
            viewModelScope.launch {
                sessionsRepo.unBookmarkSession(sessionId)
            }
    }