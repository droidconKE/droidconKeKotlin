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
import com.android254.domain.models.SessionsInformationDomainModel
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.mappers.toPresentationModel
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionsRepo: SessionsRepo,
    private val syncDataWorkManager: SyncDataWorkManager
) : ViewModel() {

    private val _selectedFilterOptions: MutableStateFlow<List<SessionsFilterOption>> =
        MutableStateFlow(emptyList())
    val selectedFilterOptions = _selectedFilterOptions.asStateFlow()

    private var filterState = SessionsFilterState()

    private val _sessionsUiState = MutableStateFlow(SessionsUiState())
    val sessionsUiState = _sessionsUiState.asStateFlow()

    private val sessionsCache = mutableListOf<Session>()

    val isRefreshing = syncDataWorkManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false
        )

    init {
        viewModelScope.launch {
            fetchAllSessions()
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
            _selectedFilterOptions.value = _selectedFilterOptions.value.toMutableList().apply {
                add(option)
            }
        }

        updateFilterState(option)
    }

    private fun updateFilterState(option: SessionsFilterOption) {
        when (option.type) {
            SessionsFilterCategory.Level -> {
                val newValue = filterState.levels.toMutableList().apply {
                    val index = this.indexOf(option.value)
                    if (index < 0) {
                        add(option.value)
                    } else {
                        removeAt(index)
                    }
                }.toList()
                filterState = filterState.copy(
                    levels = newValue
                )
            }

            SessionsFilterCategory.Topic -> {
                val newValue = filterState.topics.toMutableList().apply {
                    val index = this.indexOf(option.value)
                    if (index < 0) {
                        add(option.value)
                    } else {
                        removeAt(index)
                    }
                }.toList()
                filterState = filterState.copy(
                    topics = newValue
                )
            }

            SessionsFilterCategory.Room -> {
                val newValue = filterState.rooms.toMutableList().apply {
                    val index = this.indexOf(option.value)
                    if (index < 0) {
                        add(option.value)
                    } else {
                        removeAt(index)
                    }
                }.toList()
                filterState = filterState.copy(
                    rooms = newValue
                )
            }

            SessionsFilterCategory.SessionType -> {
                val newValue = filterState.sessionTypes.toMutableList().apply {
                    val index = this.indexOf(option.value)
                    if (index < 0) {
                        add(option.value)
                    } else {
                        removeAt(index)
                    }
                }.toList()
                filterState = filterState.copy(
                    sessionTypes = newValue
                )
            }
        }
    }

    private suspend fun fetchAllSessions() {
        updateIsLoadingState()
        if (sessionsCache.isEmpty()) {
            sessionsRepo.fetchSessionsInformation().collectLatest { sessionsInformation ->
                sessionsCache.addAll(sessionsInformation.sessions)

                updateSessionDays(sessionsInformation)
                fetchFilteredSessions()
            }
        }
    }

    private fun updateSessionDays(sessionsInformation: SessionsInformationDomainModel) {
        if (sessionsInformation.eventDays.isNotEmpty()) {
            val sessionDays = sessionsInformation.eventDays.mapIndexed { index, day -> EventDate(value = day, day = index + 1) }
            _sessionsUiState.update {
                it.copy(
                    eventDays = sessionDays,
                    selectedEventDay = if (it.selectedEventDay.value == "-1") { sessionDays.first() } else { it.selectedEventDay }
                )
            }
        }
    }

    private suspend fun fetchFilteredSessions() = withContext(Dispatchers.Default) {
        updateIsLoadingState()
        updateSessions(
            sessionsCache.asSequence().filter {
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
            }.distinctBy { it.remoteId }.toList()
        )
    }

    private fun updateSessions(sessions: List<Session>) {
        val selectedDay = _sessionsUiState.value.selectedEventDay.value
        _sessionsUiState.update {
            if (sessions.isEmpty()) {
                it.copy(
                    isEmpty = true,
                    sessions = emptyList(),
                    isEmptyMessage = "No sessions found",
                    isLoading = false
                )
            } else {
                it.copy(
                    isEmpty = false,
                    sessions = sessions.map { session -> session.toPresentationModel() }.filter { session -> session.eventDay == selectedDay },
                    isEmptyMessage = "",
                    isLoading = false
                )
            }
        }
    }

    private fun updateIsLoadingState() {
        _sessionsUiState.update {
            it.copy(isLoading = true)
        }
    }

    fun fetchSessionWithFilter() {
        viewModelScope.launch {
            fetchFilteredSessions()
        }
    }

    fun clearSelectedFilterList() {
        _selectedFilterOptions.value = listOf()
        filterState = SessionsFilterState()
        viewModelScope.launch {
            fetchFilteredSessions()
        }
    }

    fun updateSelectedDay(date: EventDate) {
        _sessionsUiState.value = _sessionsUiState.value.copy(
            selectedEventDay = date
        )
        viewModelScope.launch {
            fetchFilteredSessions()
        }
    }

    fun refreshSessionList() {
        _selectedFilterOptions.value = listOf()
        filterState = SessionsFilterState(isBookmarked = filterState.isBookmarked)
        sessionsCache.clear()
        viewModelScope.launch {
            syncDataWorkManager.startSync()
        }
    }

    suspend fun bookmarkSession(id: String) {
        sessionsRepo.bookmarkSession(id = id)
        sessionsCache.clear()
        fetchAllSessions()
    }

    suspend fun unBookmarkSession(id: String) {
        sessionsRepo.unBookmarkSession(id = id)
        sessionsCache.clear()
        fetchAllSessions()
    }

    fun toggleBookmarkFilter() {
        filterState = filterState.copy(isBookmarked = !filterState.isBookmarked)
        viewModelScope.launch {
            fetchFilteredSessions()
        }
    }
}