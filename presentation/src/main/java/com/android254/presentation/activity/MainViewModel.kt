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
package com.android254.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.repos.SessionsRepo
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.sessions.mappers.toPresentationModel
import com.android254.presentation.sessions.models.SessionUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val sessionsRepo: SessionsRepo,
        private val clock: Clock,
    ) : ViewModel() {
        private val ticker =
            flow {
                while (true) {
                    emit(clock.now())
                    delay(60000) // Refresh every minute
                }
            }

        val sessionState: StateFlow<SessionUIState> =
            ticker.flatMapLatest { now ->
                val currentTime = now.toEpochMilliseconds()
                combine(
                    sessionsRepo.fetchCurrentSessions(currentTime),
                    sessionsRepo.fetchUpNextSessions(currentTime),
                ) { current, upNext ->
                    SessionUIState(
                        current =
                            current.map {
                                it.toPresentationModel(now)
                            },
                        upNext =
                            upNext.map {
                                it.toPresentationModel(now)
                            },
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SessionUIState(),
            )
    }