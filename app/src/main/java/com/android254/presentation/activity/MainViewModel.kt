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
import com.android254.presentation.notifications.NotificationPermission
import com.android254.presentation.sessions.mappers.toPresentationModel
import com.android254.presentation.sessions.models.SessionUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import ke.droidcon.kotlin.core.common.time.ticks
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val sessionsRepo: SessionsRepo,
        private val clock: Clock,
        private val remoteFeatureToggle: RemoteFeatureToggle,
        private val syncDataWorkManager: SyncDataWorkManager,
        private val notificationPermission: NotificationPermission,
        private val notificationPreferences: NotificationPreferencesStore,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _isInitialising = MutableStateFlow(true)

        /**
         * True while the splash screen should stay up.
         *
         * Waits on locally cached data with a hard ceiling, never on the network; config
         * fetch and sync are fire-and-forget.
         */
        val isInitialising: StateFlow<Boolean> = _isInitialising.asStateFlow()

        private val permissionRequests = Channel<Unit>(Channel.CONFLATED)

        /**
         * One-shot requests to show the system notification prompt.
         *
         * Emitted once, when the user stars a session while the app cannot post and has never
         * asked from a star. Stars already present when the app starts never count, so the first
         * launch after an update does not prompt.
         */
        val notificationPermissionRequests: Flow<Unit> = permissionRequests.receiveAsFlow()

        private var permissionRequested = false

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

            viewModelScope.launch {
                var previous: Int? = null
                sessionsRepo
                    .fetchSessions()
                    .map { sessions -> sessions.count { it.isBookmarked } }
                    .distinctUntilChanged()
                    .collect { starred ->
                        val starredOneMore = previous?.let { starred > it } ?: false
                        previous = starred
                        if (starredOneMore && shouldRequestPermission()) {
                            permissionRequested = true
                            permissionRequests.trySend(Unit)
                        }
                    }
            }
        }

        /** Call when the system prompt is about to open, so a later star never asks again. */
        fun onNotificationPermissionPrompted() {
            viewModelScope.launch { notificationPreferences.markPermissionPrompted() }
        }

        private suspend fun shouldRequestPermission(): Boolean =
            !permissionRequested &&
                notificationPermission.needsRuntimeRequest() &&
                !notificationPreferences.preferences.first().permissionPrompted

        val sessionState: StateFlow<SessionUIState> =
            clock
                .ticks()
                .flatMapLatest { now ->
                    val currentTime = now.toEpochMilliseconds()
                    combine(
                        sessionsRepo.fetchCurrentSessions(currentTime),
                        sessionsRepo.fetchUpNextSessions(currentTime),
                    ) { current, upNext ->
                        SessionUIState(
                            current =
                                current
                                    .map { it.toPresentationModel(now) }
                                    .toImmutableList(),
                            upNext =
                                upNext
                                    .map { it.toPresentationModel(now) }
                                    .toImmutableList(),
                        )
                    }
                }.flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = SessionUIState(),
                )

        private companion object {
            /** Past this, show the UI with loading skeletons rather than an inert splash. */
            const val INITIALISATION_TIMEOUT_MS = 700L
        }
    }