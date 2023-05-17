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
package com.android254.presentation.speakers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.models.ResourceResult
import com.android254.domain.repos.SpeakersRepo
import com.android254.presentation.models.SpeakerUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed interface SpeakersScreenUiState {

    object Loading : SpeakersScreenUiState

    data class Success(val speakers: List<SpeakerUI>  ) : SpeakersScreenUiState

    data class Error(val message: String ) : SpeakersScreenUiState
}

@HiltViewModel
class SpeakersScreenViewModel @Inject constructor(
    private val speakersRepo: SpeakersRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow<SpeakersScreenUiState>(SpeakersScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSpeakers()
        }
    }

    suspend fun getSpeakers() {
        when (val result = speakersRepo.fetchSpeakers()) {
            is ResourceResult.Success -> {
                val speakers =  result.data?.map {
                    SpeakerUI(
                        id = 1,
                        imageUrl = it.avatar,
                        name = it.name,
                        tagline = it.tagline,
                        bio = it.biography,
                        twitterHandle = it.twitter
                    )
                } ?: emptyList()
                _uiState.value = SpeakersScreenUiState.Success(speakers = speakers)
            }
            is ResourceResult.Error -> {
                _uiState.value = SpeakersScreenUiState.Error(message = result.message)

            }
            else -> {}
        }
    }
}