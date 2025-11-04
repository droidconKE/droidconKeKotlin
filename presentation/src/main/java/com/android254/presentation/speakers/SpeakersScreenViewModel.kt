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
package com.android254.presentation.speakers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.repos.SpeakersRepo
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.models.SpeakerUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface SpeakersScreenUiState {
    object Loading : SpeakersScreenUiState

    data class Success(val speakers: List<SpeakerUI>) : SpeakersScreenUiState

    data class Error(val message: String) : SpeakersScreenUiState
}

@HiltViewModel
class SpeakersScreenViewModel
    @Inject
    constructor(
        private val speakersRepo: SpeakersRepo,
        private val syncDataWorkManager: SyncDataWorkManager,
    ) : ViewModel() {
        val isSyncing =
            syncDataWorkManager.isSyncing
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = false,
                )

        val speakersScreenUiState: StateFlow<SpeakersScreenUiState> =
            speakersRepo.fetchSpeakers()
                .map {
                    it.map {
                        SpeakerUI(
                            id = 1,
                            imageUrl = it.avatar,
                            name = it.name,
                            tagline = it.tagline,
                            bio = it.biography,
                            twitterHandle = it.twitter,
                        )
                    }
                }
                .map<List<SpeakerUI>, SpeakersScreenUiState>(SpeakersScreenUiState::Success)
                .onStart {
                    emit(SpeakersScreenUiState.Loading)
                }
                .catch {
                    emit(SpeakersScreenUiState.Error(message = "An unexpected error occurred"))
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = SpeakersScreenUiState.Loading,
                )
    }