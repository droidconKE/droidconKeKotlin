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
import com.android254.domain.schedule.upNextToday
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.sessions.mappers.toPresentationModel
import com.android254.presentation.sessions.models.SessionUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.core.common.di.ConferenceTimeZone
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import ke.droidcon.kotlin.core.common.time.ticks
import ke.droidcon.kotlin.core.notifications.NotificationPermission
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.TimeZone
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
        @ConferenceTimeZone private val conferenceTimeZone: TimeZone,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _isInitialising = MutableStateFlow(true)

        /**
         * Waits on locally cached data with a hard ceiling, never on the network; config
         * fetch and sync are fire-and-forget.
         */
        val isInitialising: StateFlow<Boolean> = _isInitialising.asStateFlow()

        private val permissionRequests = Channel<Unit>(Channel.CONFLATED)

        private val remindersOff = Channel<Unit>(Channel.CONFLATED)

        /**
         * Emitted once, when the user stars a session that has yet to start while the app cannot
         * post and has never asked from a star. Stars already present when the app starts never
         * count, so the first launch after an update does not prompt.
         */
        val notificationPermissionRequests: Flow<Unit> = permissionRequests.receiveAsFlow()

        val remindersOffEvents: Flow<Unit> = remindersOff.receiveAsFlow()

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
                var previous: Set<String>? = null
                sessionsRepo
                    .fetchSessions()
                    .map { sessions -> sessions.filter { it.isBookmarked } }
                    .distinctUntilChanged { old, new -> old.map { it.remoteId } == new.map { it.remoteId } }
                    .collect { starred ->
                        val known = previous
                        previous = starred.mapTo(mutableSetOf()) { it.remoteId }
                        val now = clock.now()
                        // A session that has started can never produce a reminder.
                        val starredUpcoming =
                            known != null &&
                                starred.any { it.remoteId !in known && (it.startInstant ?: return@any false) > now }
                        if (!starredUpcoming) return@collect

                        when {
                            shouldRequestPermission() -> {
                                permissionRequested = true
                                permissionRequests.trySend(Unit)
                            }

                            !notificationPermission.areNotificationsEnabled() -> remindersOff.trySend(Unit)
                        }
                    }
            }
        }

        /** Call when the system prompt is about to open, so a later star never asks again. */
        fun onNotificationPermissionPrompted() {
            // The dialog is already opening; leaving the screen must not drop the record of it.
            viewModelScope.launch { withContext(NonCancellable) { notificationPreferences.markPermissionPrompted() } }
        }

        /** The outcome of the system prompt; a refusal is the moment to point at settings. */
        fun onNotificationPermissionResult(granted: Boolean) {
            if (!granted) remindersOff.trySend(Unit)
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
                                    .upNextToday(now, conferenceTimeZone)
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