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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.models.ResourceResult
import com.android254.domain.repos.SessionsRepo
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.mappers.toPresentationModel
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionsRepo: SessionsRepo,
) : ViewModel() {

    private val _selectedFilterOptions: MutableStateFlow<List<SessionsFilterOption>> =
        MutableStateFlow(emptyList())
    val selectedFilterOptions = _selectedFilterOptions.asStateFlow()

    private val _filterState: MutableStateFlow<SessionsFilterState?> = MutableStateFlow(SessionsFilterState())
    private val _selectedEventDate: MutableStateFlow<EventDate> = MutableStateFlow(
        EventDate(
            LocalDate(year = 2023, monthNumber = 11, dayOfMonth = 16),
        ),
    )
    val selectedEventDate = _selectedEventDate.asStateFlow()

    private val _sessionsUiState = MutableStateFlow<SessionsUiState>(SessionsUiState.Idle)
    val sessionsUiState = _sessionsUiState.asStateFlow()
    init {
        viewModelScope.launch {
            fetchSessions(fetchFromRemote = false)
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
                val newValue = _filterState.value?.levels?.toMutableList()?.apply {
                    val index = this.indexOf(option.value)
                    if (index < 0) {
                        add(option.value)
                    } else {
                        removeAt(index)
                    }
                }?.toList()
                _filterState.value = _filterState.value?.copy(
                    levels = newValue!!,
                )
            }
            SessionsFilterCategory.Topic -> {
                val newValue = _filterState.value!!.topics.toMutableList().apply {
                    val index = this.indexOf(option.value)
                    if (index < 0) {
                        add(option.value)
                    } else {
                        removeAt(index)
                    }
                }.toList()
                _filterState.value = _filterState.value?.copy(
                    topics = newValue,
                )
            }
            SessionsFilterCategory.Room -> {
                val newValue = _filterState.value!!.rooms.toMutableList().apply {
                    val index = this.indexOf(option.value)
                    if (index < 0) {
                        add(option.value)
                    } else {
                        removeAt(index)
                    }
                }.toList()
                _filterState.value = _filterState.value?.copy(
                    rooms = newValue,
                )
            }
            SessionsFilterCategory.SessionType -> {
                val newValue = _filterState.value!!.sessionTypes.toMutableList().apply {
                    val index = this.indexOf(option.value)
                    if (index < 0) {
                        add(option.value)
                    } else {
                        removeAt(index)
                    }
                }.toList()
                _filterState.value = _filterState.value!!.copy(
                    sessionTypes = newValue,
                )
            }
        }
    }

    private suspend fun fetchSessions(query: String? = null, fetchFromRemote: Boolean = false) {
        val result =
            sessionsRepo.fetchAndSaveSessions(query = query, fetchFromRemote = fetchFromRemote)
        when (result) {
            is ResourceResult.Success -> {
                result.data.let { sessionDomainModels ->
                    val sessions = sessionDomainModels?.map { sessionDomainModel ->
                        sessionDomainModel.toPresentationModel()
                    }

                  _sessionsUiState.value =  if (!sessions.isNullOrEmpty()) {
                        SessionsUiState.Data(data = sessions)
                    } else {
                        SessionsUiState.Empty("No sessions Found")
                    }
                }
            }

            is ResourceResult.Error -> {
                _sessionsUiState.value = SessionsUiState.Error(message = result.message)
            }

            is ResourceResult.Loading -> {
                _sessionsUiState.value = SessionsUiState.Loading
            }

            is ResourceResult.Empty -> {
                _sessionsUiState.value = SessionsUiState.Empty("No sessions Found")
            }

            else -> Unit
        }

        //       var query = Query().fields("*").from("sessions")
//        if (_filterState.value!!.rooms.isNotEmpty()) {
//            val rooms = _filterState.value!!.rooms.joinToString(",")
//            query = query.where("rooms LIKE '%$rooms%'")
//        }
//
//        if (_filterState.value!!.levels.isNotEmpty()) {
//            val sessionLevels = _filterState.value!!.levels.joinToString("','", "'", "'")
//            query.where("sessionLevel IN ($sessionLevels)")
//        }
//
//        if (_filterState.value!!.sessionTypes.isNotEmpty()) {
//            val sessionTypes = _filterState.value!!.sessionTypes.joinToString("','", "'", "'")
//            query.where("sessionFormat IN ($sessionTypes)")
//        }
//
//        if (_filterState.value!!.isBookmarked) {
//            val isBookmarked = _filterState.value!!.isBookmarked
//            query.where("isBookmarked = '${if (isBookmarked) '1' else '0'}'")
//        }
//
//        query.orderAsc("startTimestamp")
//
//        return query.toSql()
    }

    fun getQuery(): String = listOf("1", "2").random()

    fun fetchSessionWithFilter() {
        viewModelScope.launch {
            fetchSessions(query = getQuery())
        }
    }

    fun clearSelectedFilterList() {
        _selectedFilterOptions.value = listOf()
        _filterState.value = SessionsFilterState()
        viewModelScope.launch {
            fetchSessions()
        }
    }

    fun updateSelectedDay(date: EventDate) {
        _selectedEventDate.value = date
        viewModelScope.launch {
            fetchSessions(query = getQuery())
        }
    }

    fun refreshSessionList() {
        _selectedFilterOptions.value = listOf()
        _filterState.value = SessionsFilterState()
        viewModelScope.launch {
            fetchSessions(fetchFromRemote = true)
        }
    }

    suspend fun updateBookmarkStatus(
        id: String,
        isCurrentlyStarred: Boolean,
    ): ResourceResult<Boolean> = sessionsRepo.toggleBookmarkStatus(id, isCurrentlyStarred)

    fun fetchBookmarkedSessions() {
        viewModelScope.launch {
            _filterState.value = SessionsFilterState()
            _filterState.value = _filterState.value?.copy(isBookmarked = true)
            fetchSessionWithFilter()
        }
    }
}