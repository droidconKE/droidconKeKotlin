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
import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.SpeakersRepo
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.sessions.mappers.toPresentationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock

sealed interface SpeakerDetailsScreenUiState {
    object Loading : SpeakerDetailsScreenUiState

    data class Success(
        val speaker: SpeakerUI,
        val sessions: ImmutableList<SessionPresentationModel> = persistentListOf(),
    ) : SpeakerDetailsScreenUiState

    data class Error(
        val message: String,
    ) : SpeakerDetailsScreenUiState

    data class SpeakerNotFound(
        val message: String,
    ) : SpeakerDetailsScreenUiState
}

@HiltViewModel
class SpeakerDetailsScreenViewModel
    @Inject
    constructor(
        private val speakersRepo: SpeakersRepo,
        private val sessionsRepo: SessionsRepo,
        private val clock: Clock,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SpeakerDetailsScreenUiState>(SpeakerDetailsScreenUiState.Loading)
        val uiState = _uiState.asStateFlow()

        suspend fun getSpeakerByName(name: String) {
            combine(
                speakersRepo.getSpeakerByName(name),
                sessionsRepo.fetchSessions(),
            ) { speaker, sessions ->
                SpeakerDetailsScreenUiState.Success(
                    speaker = speaker.toPresentation(),
                    sessions = sessions.sessionsFor(speaker),
                )
            }.flowOn(ioDispatcher)
                .catch {
                    _uiState.value = SpeakerDetailsScreenUiState.Error(message = "An unexpected error occurred")
                }.collect { _uiState.value = it }
        }

        fun onBookmark(sessionId: String) {
            val current = _uiState.value
            if (current !is SpeakerDetailsScreenUiState.Success) return
            val session = current.sessions.find { it.id == sessionId } ?: return
            viewModelScope.launch {
                if (session.isStarred) {
                    sessionsRepo.unBookmarkSession(session.remoteId)
                } else {
                    sessionsRepo.bookmarkSession(session.remoteId)
                }
            }
        }

        /**
         * There is no sessions-by-speaker query, and [Speaker] carries no id, so the only link
         * available is the speaker list each [Session] already embeds — matched on name.
         */
        private fun List<Session>.sessionsFor(speaker: Speaker): ImmutableList<SessionPresentationModel> {
            val now = clock.now()
            return this
                .filter { session -> session.speakers.any { it.name.equals(speaker.name, ignoreCase = true) } }
                .distinctBy { it.remoteId }
                .map { it.toPresentationModel(now) }
                .toImmutableList()
        }

        private fun Speaker.toPresentation() =
            SpeakerUI(
                id = 1,
                imageUrl = avatar,
                name = name,
                tagline = tagline,
                bio = biography,
                twitterHandle = twitter,
            )
    }