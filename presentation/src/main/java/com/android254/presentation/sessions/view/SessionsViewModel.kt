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
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.mappers.toPresentationModel
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import javax.inject.Inject

data class Error(
    val message: String,
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionsRepo: SessionsRepo,
) : ViewModel() {
    private val _sessions: MutableStateFlow<List<SessionPresentationModel>> = MutableStateFlow(emptyList())
    private val _displayableSessions: MutableStateFlow<List<SessionPresentationModel>?> = MutableStateFlow(emptyList())

    private val _error = MutableStateFlow<Error?>(null)
    private val _loading = MutableStateFlow(false)
    private val _empty = MutableStateFlow(false)
    private val _selectedFilterOptions: MutableStateFlow<List<SessionsFilterOption>> = MutableStateFlow(emptyList())
    private val _filterState: MutableStateFlow<SessionsFilterState?> = MutableStateFlow(SessionsFilterState())

    private val _selectedEventDate: MutableStateFlow<EventDate> = MutableStateFlow(
        EventDate(
            LocalDate(year = 2023, monthNumber = 11, dayOfMonth = 16),
        ),
    )

    val selectedEventDate = _selectedEventDate.asStateFlow()
    val sessions = _displayableSessions.asStateFlow()

    val error = _error.asStateFlow()
    val loading = _loading.asStateFlow()
    val empty = _empty.asStateFlow()
    val selectedFilterOptions = _selectedFilterOptions.asStateFlow()

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
        when (
            val result =
                sessionsRepo.fetchAndSaveSessions(query = query, fetchFromRemote = fetchFromRemote)
        ) {
            is ResourceResult.Success -> {
                result.data.let { sessionDomainModels ->
                    _sessions.value = sessionDomainModels?.map { sessionDomainModel ->
                        sessionDomainModel.toPresentationModel()
                    }!!

                    _displayableSessions.value =
                        sessionDomainModels.map { sessionDomainModel ->
                            sessionDomainModel.toPresentationModel()
                        }.filter {
                            it.startDate.split(" ").first()
                                .toLocalDate().dayOfMonth == selectedEventDate.value.value.dayOfMonth
                        }
                }
            }

            is ResourceResult.Error -> {
                _error.value = Error(result.message)
            }

            is ResourceResult.Loading -> {
                _loading.value = result.isLoading
            }

            is ResourceResult.Empty -> {
                _empty.value = true
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