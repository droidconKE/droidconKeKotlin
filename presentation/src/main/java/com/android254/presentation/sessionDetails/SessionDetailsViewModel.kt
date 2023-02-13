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
package com.android254.presentation.sessionDetails

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.models.ResourceResult
import com.android254.domain.repos.SessionsRepo
import com.android254.presentation.models.SessionDetailsPresentationModel
import com.android254.presentation.sessions.mappers.toSessionDetailsPresentationModal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionDetailsViewModel @Inject constructor(
    private val sessionsRepo: SessionsRepo
) : ViewModel() {
    private val _sessionDetails: MutableLiveData<SessionDetailsPresentationModel> =
        MutableLiveData(null)

    var sessionDetails: LiveData<SessionDetailsPresentationModel> = _sessionDetails

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSessionDetailsById(sessionId: String) {
        viewModelScope.launch {
            sessionsRepo.fetchSessionById(sessionId).collectLatest { result ->
                when (result) {
                    is ResourceResult.Success -> {
                        result.data.let {
                            _sessionDetails.value = it?.toSessionDetailsPresentationModal()
                        }
                    }

                    is ResourceResult.Error -> {
                    }

                    is ResourceResult.Loading -> {
                    }

                    is ResourceResult.Empty -> {
                    }

                    else -> Unit
                }
            }
        }
    }
}