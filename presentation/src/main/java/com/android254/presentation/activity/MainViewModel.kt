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
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.sessions.mappers.toPresentationModel
import com.android254.presentation.sessions.models.SessionUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val sessionsRepo: SessionsRepo,
        private val clock: Clock,
        private val remoteFeatureToggle: RemoteFeatureToggle,
        private val syncDataWorkManager: SyncDataWorkManager,
    ) : ViewModel() {
        private val _isInitialising = MutableStateFlow(true)

        /**
         * True while the splash screen should stay up.
         *
         * Waits on locally cached data with a hard ceiling, never on the network; config
         * fetch and sync are fire-and-forget.
         */
        val isInitialising: StateFlow<Boolean> = _isInitialising.asStateFlow()

        init {
            viewModelScope.launch {
                withTimeoutOrNull(INITIALISATION_TIMEOUT_MS) {
                    sessionsRepo.fetchSessions().first()
                }
                _isInitialising.value = false
            }

            viewModelScope.launch {
                runCatching { remoteFeatureToggle.syncNowIfEmpty() }
                    .onSuccess { shouldSync -> if (shouldSync) syncDataWorkManager.startSync() }
                    .onFailure { Timber.w(it, "Feature toggle fetch failed; using cached config") }
            }
        }

        private val ticker =
            flow {
                while (true) {
                    emit(clock.now())
                    delay(TICK_INTERVAL_MS)
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

        private companion object {
            /** Past this, show the UI with loading skeletons rather than an inert splash. */
            const val INITIALISATION_TIMEOUT_MS = 700L
            const val TICK_INTERVAL_MS = 60_000L
        }
    }