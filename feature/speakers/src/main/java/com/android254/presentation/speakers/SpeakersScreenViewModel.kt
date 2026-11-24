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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.SpeakersRepo
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.models.SpeakerUI
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Clock

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
        private val sessionsRepo: SessionsRepo,
        private val syncDataWorkManager: SyncDataWorkManager,
        private val clock: Clock,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        val isSyncing =
            syncDataWorkManager.isSyncing
                .flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = false,
                )

        val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")

        fun onSearchQueryChanged(query: String) {
            savedStateHandle[KEY_SEARCH_QUERY] = query
        }

        private val speakingNowNames: Flow<Set<String>> =
            flow {
                while (true) {
                    emit(clock.now().toEpochMilliseconds())
                    delay(TICK_INTERVAL_MS)
                }
            }.flatMapLatest { now ->
                sessionsRepo.fetchCurrentSessions(now).map { sessions ->
                    sessions
                        .flatMap { it.speakers }
                        .mapTo(mutableSetOf()) { it.name.trim().lowercase() }
                }
            }

        val speakersScreenUiState: StateFlow<SpeakersScreenUiState> =
            combine(
                speakersRepo.fetchSpeakers(),
                searchQuery,
                speakingNowNames,
            ) { speakers, query, speakingNow ->
                speakers
                    .map { speaker ->
                        SpeakerUI(
                            id = 1,
                            imageUrl = speaker.avatar,
                            name = speaker.name,
                            tagline = speaker.tagline,
                            bio = speaker.biography,
                            twitterHandle = speaker.twitter,
                            isSpeakingNow = speaker.name.trim().lowercase() in speakingNow,
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

        private companion object {
            const val TICK_INTERVAL_MS = 60_000L
            const val KEY_SEARCH_QUERY = "speakers_search_query"
        }
    }

private fun SpeakerUI.matches(query: String): Boolean {
    val trimmed = query.trim()
    if (trimmed.isBlank()) return true
    return listOfNotNull(name, tagline, bio, twitterHandle)
        .any { it.contains(trimmed, ignoreCase = true) }
}