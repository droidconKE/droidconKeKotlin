/*
 * Copyright 2026 DroidconKE
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
package com.android254.presentation.ticket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.repos.AddTicketResult
import com.android254.domain.repos.TicketsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.core.common.di.ConferenceTimeZone
import ke.droidcon.kotlin.core.common.time.ticks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
class TicketViewModel
    @Inject
    constructor(
        private val ticketsRepo: TicketsRepo,
        clock: Clock,
        @ConferenceTimeZone private val zone: TimeZone,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val form =
            MutableStateFlow(AddTicketForm(number = savedStateHandle[TYPED_NUMBER] ?: "", email = savedStateHandle[TYPED_EMAIL] ?: ""))

        val uiState: StateFlow<TicketUiState> =
            combine(ticketsRepo.ticket(), form, clock.ticks()) { ticket, form, now ->
                TicketUiState.Ready(ticket = ticket?.toUi(now, zone), form = form)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TicketUiState.Loading)

        init {
            viewModelScope.launch { ticketsRepo.refresh() }
        }

        fun onAction(action: TicketAction) {
            when (action) {
                is TicketAction.NumberChanged -> {
                    savedStateHandle[TYPED_NUMBER] = action.value
                    form.update { it.copy(number = action.value, error = null) }
                }
                is TicketAction.EmailChanged -> {
                    savedStateHandle[TYPED_EMAIL] = action.value
                    form.update { it.copy(email = action.value, error = null) }
                }
                TicketAction.Add -> form.value.takeIf { it.canSubmit }?.let { add { ticketsRepo.add(it.number, it.email) } }
                TicketAction.Remove -> viewModelScope.launch { ticketsRepo.remove() }
                TicketAction.Scan -> Unit
            }
        }

        fun onScanned(code: String) = add { ticketsRepo.addScanned(code) }

        fun onScannerUnavailable() = form.update { it.copy(error = AddTicketError.ScannerUnavailable) }

        private fun add(lookUp: suspend () -> AddTicketResult) {
            form.update { it.copy(isAdding = true, error = null) }
            viewModelScope.launch {
                when (val result = lookUp()) {
                    is AddTicketResult.Added -> {
                        savedStateHandle[TYPED_NUMBER] = ""
                        savedStateHandle[TYPED_EMAIL] = ""
                        form.value = AddTicketForm()
                    }
                    else -> form.update { it.copy(isAdding = false, error = result.toError()) }
                }
            }
        }
    }

private const val TYPED_NUMBER = "typed_number"
private const val TYPED_EMAIL = "typed_email"

private fun AddTicketResult.toError(): AddTicketError =
    when (this) {
        AddTicketResult.NoMatch -> AddTicketError.NoMatch
        AddTicketResult.NotATicket -> AddTicketError.NotATicket
        AddTicketResult.Offline -> AddTicketError.Offline
        AddTicketResult.Busy -> AddTicketError.Busy
        AddTicketResult.Failed, is AddTicketResult.Added -> AddTicketError.Failed
    }