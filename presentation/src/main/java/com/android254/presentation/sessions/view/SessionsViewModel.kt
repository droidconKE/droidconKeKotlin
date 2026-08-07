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
package com.android254.presentation.sessions.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.models.Session
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.common.resultstatus.ResultStatus
import com.android254.presentation.di.ConferenceTimeZone
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.mappers.toPresentationModel
import com.android254.presentation.sessions.models.SessionsIntentHandler
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
class SessionsViewModel
    @Inject
    constructor(
        private val sessionsRepo: SessionsRepo,
        private val syncDataWorkManager: SyncDataWorkManager,
        private val clock: Clock,
        @ConferenceTimeZone private val conferenceTimeZone: TimeZone,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _selectedFilterOptions: MutableStateFlow<PersistentList<SessionsFilterOption>> =
            MutableStateFlow(persistentListOf())
        val selectedFilterOptions = _selectedFilterOptions.asStateFlow()

        private val _filterState = MutableStateFlow(SessionsFilterState())
        val filterState = _filterState.asStateFlow()

        private val _selectedEventDay = MutableStateFlow(EventDate(UNSET_EVENT_DAY, 1))
        val selectedEventDay = _selectedEventDay.asStateFlow()

        val sessionsUiState =
            flow {
                emitAll(sessionsRepo.fetchSessionsInformation())
            }.flatMapLatest { sessionsInformation ->
                combine(
                    _selectedEventDay,
                    _filterState,
                ) { selectedEventDay, filterState ->
                    val sessionDays = mapEventDays(sessionsInformation.eventDays)

                    if (selectedEventDay.value == UNSET_EVENT_DAY && sessionDays.isNotEmpty()) {
                        _selectedEventDay.value = defaultEventDay(sessionDays)
                    }

                    val filteredSessions =
                        filterSessions(
                            sessionsInformation.sessions,
                            filterState,
                            _selectedEventDay.value,
                        )

                    SessionsUiState(
                        sessions = filteredSessions.toImmutableList(),
                        eventDays = sessionDays.toImmutableList(),
                        sessionStatus = getResultStatus(filteredSessions),
                        availableFilters = buildFilterOptions(sessionsInformation.sessions).toImmutableList(),
                        showMySessionsOnly = filterState.isBookmarked,
                        isFilterActive = filterState.isActive,
                    )
                }
            }.flowOn(ioDispatcher)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SessionsUiState(),
            )

        /** Today if the conference is running, otherwise the first day. */
        private fun defaultEventDay(eventDays: List<EventDate>): EventDate {
            val today = clock.now().toLocalDateTime(conferenceTimeZone).day
            return eventDays.find { it.value.toIntOrNull() == today } ?: eventDays.first()
        }

        private fun mapEventDays(eventDays: List<String>): List<EventDate> =
            eventDays.mapIndexed { index, day ->
                EventDate(value = day, day = index + 1)
            }

        private fun filterSessions(
            sessions: List<Session>,
            filterState: SessionsFilterState,
            selectedEventDay: EventDate,
        ): List<SessionPresentationModel> {
            val now = clock.now()
            return sessions
                .asSequence()
                .filter(filterState::matches)
                .distinctBy { it.remoteId }
                .map { it.toPresentationModel(now) }
                .filter { it.eventDay == selectedEventDay.value }
                .toList()
        }

        /**
         * Builds the selectable filter options from the sessions themselves, so an option
         * can never name a value no session has.
         *
         * Labels are the API values rather than string resources: room and format names are
         * data, not UI copy. Category headings stay localised via
         * [SessionsFilterCategory.resId].
         */
        internal fun buildFilterOptions(sessions: List<Session>): List<SessionsFilterOption> {
            fun options(
                category: SessionsFilterCategory,
                values: Sequence<String>,
            ): List<SessionsFilterOption> =
                values
                    .map(String::trim)
                    .filter(String::isNotEmpty)
                    .distinctBy(String::lowercase)
                    .sorted()
                    .map { SessionsFilterOption(type = category, label = it, value = it) }
                    .toList()

            return options(SessionsFilterCategory.Room, sessions.asSequence().flatMap { it.roomList }) +
                options(SessionsFilterCategory.SessionType, sessions.asSequence().map { it.sessionFormat }) +
                options(SessionsFilterCategory.Level, sessions.asSequence().map { it.sessionLevel })
        }

        private fun getResultStatus(sessions: List<SessionPresentationModel>): ResultStatus =
            if (sessions.isEmpty()) {
                ResultStatus.Empty("No sessions found")
            } else {
                ResultStatus.Success
            }

        val isRefreshing =
            syncDataWorkManager.isSyncing
                .flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = false,
                )

        fun handleEvent(intent: SessionsIntentHandler) {
            when (intent) {
                SessionsIntentHandler.ClearSelectedFilterList -> clearSelectedFilterList()
                SessionsIntentHandler.RefreshSessions -> refreshSessionList()
                SessionsIntentHandler.ToggleBookmarkFilter -> toggleBookmarkFilter()
                SessionsIntentHandler.Retry -> {}
                is SessionsIntentHandler.UpdateSelectedDay -> updateSelectedDay(intent.day)
                is SessionsIntentHandler.BookmarkSession -> {
                    viewModelScope.launch {
                        val session = sessionsUiState.value.sessions.find { it.id == intent.sessionId }
                        if (session != null) {
                            if (session.isStarred) unBookmarkSession(session.remoteId) else bookmarkSession(session.remoteId)
                        }
                    }
                }

                is SessionsIntentHandler.UpdateSelectedFilterOptionList -> updateSelectedFilterOptionList(intent.option)
            }
        }

        fun updateSelectedFilterOptionList(option: SessionsFilterOption) {
            _selectedFilterOptions.update { selected ->
                if (option in selected) selected.removing(option) else selected.adding(option)
            }
            _filterState.update { it.toggle(option) }
        }

        fun clearSelectedFilterList() {
            _selectedFilterOptions.value = persistentListOf()
            _filterState.value = SessionsFilterState()
        }

        fun updateSelectedDay(date: EventDate) {
            _selectedEventDay.value = date
        }

        fun refreshSessionList() {
            _selectedFilterOptions.value = persistentListOf()
            _filterState.update {
                SessionsFilterState(isBookmarked = it.isBookmarked)
            }
            viewModelScope.launch {
                syncDataWorkManager.startSync()
            }
        }

        fun bookmarkSession(id: String) {
            viewModelScope.launch {
                sessionsRepo.bookmarkSession(id = id)
            }
        }

        fun unBookmarkSession(id: String) {
            viewModelScope.launch {
                sessionsRepo.unBookmarkSession(id = id)
            }
        }

        fun toggleBookmarkFilter() {
            _filterState.update {
                it.copy(isBookmarked = !it.isBookmarked)
            }
        }
    }

/** Sentinel for "no day chosen yet", replaced on first load by [EventDate]. */
private const val UNSET_EVENT_DAY = "-1"
