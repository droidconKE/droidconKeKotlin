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
        private val scanner: TicketScanner,
        clock: Clock,
        @ConferenceTimeZone private val zone: TimeZone,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val typedNumber = savedStateHandle.getStateFlow(TYPED_NUMBER, "")
        private val typedEmail = savedStateHandle.getStateFlow(TYPED_EMAIL, "")
        private val lookUp = MutableStateFlow(LookUp())

        val uiState: StateFlow<TicketUiState> =
            combine(ticketsRepo.ticket(), typedNumber, typedEmail, lookUp, clock.ticks()) { ticket, number, email, lookUp, now ->
                TicketUiState.Ready(
                    ticket = ticket?.toUi(now, zone),
                    form = AddTicketForm(number = number, email = email, isAdding = lookUp.inProgress, error = lookUp.error),
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TicketUiState.Loading)

        init {
            viewModelScope.launch { ticketsRepo.refresh() }
        }

        fun onAction(action: TicketAction) {
            when (action) {
                is TicketAction.NumberChanged -> type(TYPED_NUMBER, action.value)
                is TicketAction.EmailChanged -> type(TYPED_EMAIL, action.value)
                TicketAction.Add -> {
                    val form = AddTicketForm(typedNumber.value, typedEmail.value, lookUp.value.inProgress)
                    if (form.canSubmit) add { ticketsRepo.add(form.number, form.email) }
                }
                TicketAction.Scan ->
                    if (!lookUp.value.inProgress) {
                        add {
                            when (val scanned = scanner.scan()) {
                                is ScanResult.Code -> ticketsRepo.addScanned(scanned.value)
                                ScanResult.Cancelled -> null
                                ScanResult.Installing -> AddTicketResult.ScannerInstalling
                                ScanResult.Unavailable -> AddTicketResult.ScannerUnavailable
                            }
                        }
                    }
                TicketAction.Remove -> viewModelScope.launch { ticketsRepo.remove() }
            }
        }

        private fun type(
            key: String,
            value: String,
        ) {
            savedStateHandle[key] = value
            lookUp.update { it.copy(error = null) }
        }

        /** Null: a scan the attendee backed out of. */
        private fun add(lookUpTicket: suspend () -> AddTicketResult?) {
            lookUp.value = LookUp(inProgress = true)
            viewModelScope.launch {
                when (val result = lookUpTicket()) {
                    is AddTicketResult.Added -> {
                        savedStateHandle[TYPED_NUMBER] = ""
                        savedStateHandle[TYPED_EMAIL] = ""
                        lookUp.value = LookUp()
                    }
                    null -> lookUp.value = LookUp()
                    else -> lookUp.value = LookUp(error = result.toError())
                }
            }
        }

        private data class LookUp(
            val inProgress: Boolean = false,
            val error: AddTicketError? = null,
        )
    }

private const val TYPED_NUMBER = "typed_number"
private const val TYPED_EMAIL = "typed_email"

private fun AddTicketResult.toError(): AddTicketError =
    when (this) {
        AddTicketResult.NoMatch -> AddTicketError.NoMatch
        AddTicketResult.NotATicket -> AddTicketError.NotATicket
        AddTicketResult.Offline -> AddTicketError.Offline
        AddTicketResult.Busy -> AddTicketError.Busy
        AddTicketResult.ScannerInstalling -> AddTicketError.ScannerInstalling
        AddTicketResult.ScannerUnavailable -> AddTicketError.ScannerUnavailable
        AddTicketResult.Failed, is AddTicketResult.Added -> AddTicketError.Failed
    }