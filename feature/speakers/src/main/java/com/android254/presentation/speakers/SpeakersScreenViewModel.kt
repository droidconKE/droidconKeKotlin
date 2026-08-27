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
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface SpeakersScreenUiState {
    object Loading : SpeakersScreenUiState

    data class Success(
        val speakers: ImmutableList<SpeakerUI>,
    ) : SpeakersScreenUiState

    data class Error(
        val message: String,
    ) : SpeakersScreenUiState
}

@HiltViewModel
class SpeakersScreenViewModel
    @Inject
    constructor(
        private val speakersRepo: SpeakersRepo,
        private val syncDataWorkManager: SyncDataWorkManager,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        val isSyncing =
            syncDataWorkManager.isSyncing
                .flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = false,
                )

        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

        fun onSearchQueryChanged(query: String) {
            _searchQuery.value = query
        }

        val speakersScreenUiState: StateFlow<SpeakersScreenUiState> =
            combine(
                speakersRepo.fetchSpeakers(),
                _searchQuery,
            ) { speakers, query ->
                speakers
                    .map { speaker ->
                        SpeakerUI(
                            id = 1,
                            imageUrl = speaker.avatar,
                            name = speaker.name,
                            tagline = speaker.tagline,
                            bio = speaker.biography,
                            twitterHandle = speaker.twitter,
                        )
                    }.filter { it.matches(query) }
                    .toImmutableList()
            }.map<ImmutableList<SpeakerUI>, SpeakersScreenUiState>(SpeakersScreenUiState::Success)
                .onStart {
                    emit(SpeakersScreenUiState.Loading)
                }.catch {
                    emit(SpeakersScreenUiState.Error(message = "An unexpected error occurred"))
                }.flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = SpeakersScreenUiState.Loading,
                )
    }

/**
 * Free-text match across the fields a speaker is likely to be looked up by.
 * A blank query matches everything, so an inactive search bar is a no-op.
 */
private fun SpeakerUI.matches(query: String): Boolean {
    val trimmed = query.trim()
    if (trimmed.isBlank()) return true
    return listOfNotNull(name, tagline, bio, twitterHandle)
        .any { it.contains(trimmed, ignoreCase = true) }
}