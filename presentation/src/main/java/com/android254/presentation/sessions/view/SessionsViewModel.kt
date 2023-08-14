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
import com.android254.domain.models.ResourceResult
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.mappers.toPresentationModel
import com.android254.presentation.sessions.models.SessionsUiState
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionsRepo: SessionsRepo,
    private val syncDataWorkManager: SyncDataWorkManager,

) : ViewModel() {

    private val _selectedFilterOptions: MutableStateFlow<List<SessionsFilterOption>> =
        MutableStateFlow(emptyList())
    val selectedFilterOptions = _selectedFilterOptions.asStateFlow()

    private val _filterState: MutableStateFlow<SessionsFilterState?> = MutableStateFlow(SessionsFilterState())
    private val _selectedEventDate: MutableStateFlow<EventDate> = MutableStateFlow(
        EventDate(
            LocalDate(year = 2023, monthNumber = 11, dayOfMonth = 16)
        )
    )
    val selectedEventDate = _selectedEventDate.asStateFlow()

    private val _sessionsUiState = MutableStateFlow<SessionsUiState>(SessionsUiState.Idle)
    val sessionsUiState = _sessionsUiState.asStateFlow()

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
                val newValue = _filterState.value?.levels?.toMutableList()?.apply {
                    val index = this.indexOf(option.value)
                    if (index < 0) {
                        add(option.value)
                    } else {
                        removeAt(index)
                    }
                }?.toList()
                _filterState.value = _filterState.value?.copy(
                    levels = newValue!!
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
                    topics = newValue
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
                    rooms = newValue
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
                    sessionTypes = newValue
                )
            }
        }
    }

    private suspend fun fetchAllSessions() {
        _sessionsUiState.value = SessionsUiState.Loading
        sessionsRepo.fetchSessions().collectLatest { sessions ->
            try {
                _sessionsUiState.value = if (sessions.isNotEmpty()) {
                    SessionsUiState.Data(data = sessions.map { it.toPresentationModel() })
                } else {
                    SessionsUiState.Empty("No sessions Found")
                }
            }catch (e:Exception){
                _sessionsUiState.value = SessionsUiState.Error(message =
                e.message ?:"An unexpected error occurred")
            }
        }
    }

    private suspend fun fetchFilteredSessions(query:String){
        _sessionsUiState.value = SessionsUiState.Loading
        sessionsRepo.fetchFilteredSessions(query = query).collectLatest { sessions ->
            try {
                _sessionsUiState.value = if (sessions.isNotEmpty()) {
                    SessionsUiState.Data(data = sessions.map { it.toPresentationModel() })
                } else {
                    SessionsUiState.Empty("No sessions Found")
                }
            }catch (e:Exception){
                _sessionsUiState.value = SessionsUiState.Error(message =
                e.message ?:"An unexpected error occurred")
            }
        }
    }

    private suspend fun fetchBookmarkSessions(){
        _sessionsUiState.value = SessionsUiState.Loading
        sessionsRepo.fetchBookmarkedSessions().collectLatest { sessions ->
            try {
                _sessionsUiState.value = if (sessions.isNotEmpty()) {
                    SessionsUiState.Data(data = sessions.map { it.toPresentationModel() })
                } else {
                    SessionsUiState.Empty("No bookmarked sessions Found")
                }
            }catch (e:Exception){
                _sessionsUiState.value = SessionsUiState.Error(message =
                e.message ?:"An unexpected error occurred")
            }
        }
    }


    private fun getQuery(): String {
        val separator = ","
        val prefix = "("
        val postfix = ")"
        val stringBuilder = StringBuilder()
        _filterState.value?.let {
            if (it.levels.isNotEmpty()) {
                val items =
                    it.levels.joinToString(separator, prefix, postfix) { value -> value.lowercase() }
                stringBuilder.append("LOWER (sessionLevel) IN $items")
            }
            if (it.sessionTypes.isNotEmpty()) {
                val items = it.sessionTypes.joinToString(
                    separator,
                    prefix,
                    postfix
                ) { value -> "'${value.lowercase()}'" }
                if (stringBuilder.isNotEmpty()) stringBuilder.append(" AND ")
                stringBuilder.append("LOWER (sessionFormat) IN $items")
            }
            if (it.rooms.isNotEmpty()) {
                val items =
                    it.rooms.joinToString(separator, prefix, postfix) { value -> value.lowercase() }
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.append(" AND ")
                }
                stringBuilder.append("LOWER (rooms) IN $items")
            }
        }
        val where = if (stringBuilder.isNotEmpty()) {
            "WHERE $stringBuilder"
        } else {
            stringBuilder
        }
        return "SELECT * FROM sessions $where".also { Timber.i("QUERY = $it") }
    }

    fun fetchSessionWithFilter() {
        viewModelScope.launch {
            fetchFilteredSessions(query = getQuery())
        }
    }

    fun clearSelectedFilterList() {
        _selectedFilterOptions.value = listOf()
        _filterState.value = SessionsFilterState()
        viewModelScope.launch {
            fetchAllSessions()
        }
    }

    fun updateSelectedDay(date: EventDate) {
        _selectedEventDate.value = date
        viewModelScope.launch {
            fetchFilteredSessions(query = getQuery())
        }
    }

    fun refreshSessionList() {
        _selectedFilterOptions.value = listOf()
        _filterState.value = SessionsFilterState()
        viewModelScope.launch {
            syncDataWorkManager.startSync()
        }
    }

    suspend fun bookmarkSession(id:String){
        sessionsRepo.bookmarkSession(id = id)
    }

    suspend fun unBookmarkSession(id:String){
        sessionsRepo.unBookmarkSession(id = id)
    }

    fun toggleBookmarkFilter() {
        viewModelScope.launch {
            _filterState.value = SessionsFilterState()
            val previousState = _filterState?.value?.isBookmarked ?: false
            _filterState.value = _filterState.value?.copy(isBookmarked = !previousState)
            if (_filterState?.value?.isBookmarked == true){
                fetchBookmarkSessions()
            }else{
                fetchAllSessions()
            }
        }
    }
}