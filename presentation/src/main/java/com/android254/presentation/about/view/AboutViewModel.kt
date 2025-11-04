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
package com.android254.presentation.about.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.repos.OrganizersRepo
import com.android254.presentation.models.OrganizingTeamMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface AboutScreenUiState {
    object Loading : AboutScreenUiState

    data class Success(
        val teamMembers: List<OrganizingTeamMember>,
        val stakeHoldersLogos: List<String>,
    ) : AboutScreenUiState

    data class Error(val message: String) : AboutScreenUiState
}

@HiltViewModel
class AboutViewModel
    @Inject
    constructor(
        private val organizersRepo: OrganizersRepo,
    ) : ViewModel() {
        val uiState =
            organizersRepo.getOrganizers()
                .map { organizers ->
                    val team =
                        organizers.filter { it.type == "individual" }.map {
                            OrganizingTeamMember(
                                name = it.name,
                                desc = it.tagline,
                                image = it.photo,
                            )
                        }
                    val stakeholders = organizers.filterNot { it.type == "individual" }.map { it.photo }
                    AboutScreenUiState.Success(teamMembers = team, stakeHoldersLogos = stakeholders)
                }
                .onStart { AboutScreenUiState.Loading }
                .catch { AboutScreenUiState.Error(message = "An unexpected error occurred") }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = AboutScreenUiState.Loading,
                )
    }