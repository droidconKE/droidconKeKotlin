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
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.mappers.toPresentationModel
import com.android254.presentation.sessions.models.SessionsIntentHandler
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel
    @Inject
    constructor(
        private val sessionsRepo: SessionsRepo,
        private val syncDataWorkManager: SyncDataWorkManager,
    ) : ViewModel() {
        private val _selectedFilterOptions: MutableStateFlow<List<SessionsFilterOption>> =
            MutableStateFlow(emptyList())
        val selectedFilterOptions = _selectedFilterOptions.asStateFlow()

        private val _filterState = MutableStateFlow(SessionsFilterState())
        val filterState = _filterState.asStateFlow()

        private val _selectedEventDay = MutableStateFlow(EventDate("-1", 1))
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

                    // Update selected day if not set
                    if (selectedEventDay.value == "-1" && sessionDays.isNotEmpty()) {
                        val currentDay = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
                        val defaultSelectedDay = sessionDays.find { it.value == currentDay } ?: sessionDays.first()
                        _selectedEventDay.value = defaultSelectedDay
                    }

                    val filteredSessions =
                        filterSessions(
                            sessionsInformation.sessions,
                            filterState,
                            _selectedEventDay.value,
                        )

                    SessionsUiState(
                        sessions = filteredSessions,
                        eventDays = sessionDays,
                        sessionStatus = getResultStatus(filteredSessions),
                    )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SessionsUiState(),
            )

        private fun mapEventDays(eventDays: List<String>): List<EventDate> =
            eventDays.mapIndexed { index, day ->
                EventDate(value = day, day = index + 1)
            }

        private fun filterSessions(
            sessions: List<Session>,
            filterState: SessionsFilterState,
            selectedEventDay: EventDate,
        ): List<SessionPresentationModel> =
            sessions.asSequence().filter {
                if (filterState.levels.isNotEmpty()) {
                    filterState.levels.contains(it.sessionLevel)
                } else {
                    true
                }
            }.filter {
                if (filterState.rooms.isNotEmpty()) {
                    filterState.rooms.contains(it.rooms)
                } else {
                    true
                }
            }.filter {
                if (filterState.sessionTypes.isNotEmpty()) {
                    filterState.sessionTypes.contains(it.sessionFormat)
                } else {
                    true
                }
            }.filter {
                if (filterState.isBookmarked) {
                    it.isBookmarked
                } else {
                    true
                }
            }.distinctBy { it.remoteId }
                .map { it.toPresentationModel() }
                .filter { it.eventDay == selectedEventDay.value }.toList()

        private fun getResultStatus(sessions: List<SessionPresentationModel>): ResultStatus =
            if (sessions.isEmpty()) {
                ResultStatus.Empty("No sessions found")
            } else {
                ResultStatus.Success
            }

        val isRefreshing =
            syncDataWorkManager.isSyncing
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
            if (_selectedFilterOptions.value.contains(option)) {
                val index = _selectedFilterOptions.value.indexOf(option)
                _selectedFilterOptions.value =
                    _selectedFilterOptions.value.toMutableList().apply {
                        removeAt(index)
                    }
            } else {
                _selectedFilterOptions.value =
                    _selectedFilterOptions.value.toMutableList().apply {
                        add(option)
                    }
            }

            updateFilterState(option)
        }

        private fun updateFilterState(option: SessionsFilterOption) {
            when (option.type) {
                SessionsFilterCategory.Level -> {
                    val newValue =
                        _filterState.value.levels.toMutableList().apply {
                            val index = this.indexOf(option.value)
                            if (index < 0) {
                                add(option.value)
                            } else {
                                removeAt(index)
                            }
                        }.toList()
                    _filterState.update {
                        it.copy(
                            levels = newValue,
                        )
                    }
                }

                SessionsFilterCategory.Topic -> {
                    val newValue =
                        _filterState.value.topics.toMutableList().apply {
                            val index = this.indexOf(option.value)
                            if (index < 0) {
                                add(option.value)
                            } else {
                                removeAt(index)
                            }
                        }.toList()
                    _filterState.update {
                        it.copy(
                            topics = newValue,
                        )
                    }
                }

                SessionsFilterCategory.Room -> {
                    val newValue =
                        _filterState.value.rooms.toMutableList().apply {
                            val index = this.indexOf(option.value)
                            if (index < 0) {
                                add(option.value)
                            } else {
                                removeAt(index)
                            }
                        }.toList()
                    _filterState.update {
                        it.copy(
                            rooms = newValue,
                        )
                    }
                }

                SessionsFilterCategory.SessionType -> {
                    val newValue =
                        _filterState.value.sessionTypes.toMutableList().apply {
                            val index = this.indexOf(option.value)
                            if (index < 0) {
                                add(option.value)
                            } else {
                                removeAt(index)
                            }
                        }.toList()
                    _filterState.update {
                        it.copy(
                            sessionTypes = newValue,
                        )
                    }
                }
            }
        }

        fun clearSelectedFilterList() {
            _selectedFilterOptions.value = listOf()
            _filterState.value = SessionsFilterState()
        }

        fun updateSelectedDay(date: EventDate) {
            _selectedEventDay.value = date
        }

        fun refreshSessionList() {
            _selectedFilterOptions.value = listOf()
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