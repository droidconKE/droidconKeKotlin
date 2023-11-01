/*
 * Copyright 2023 DroidconKE
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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.android254.domain.models.ResourceResult
import com.android254.domain.repos.SpeakersRepo
import com.android254.presentation.models.SpeakerUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed interface SpeakerDetailsScreenUiState {

    object Loading : SpeakerDetailsScreenUiState

    data class Success(val speaker: SpeakerUI) : SpeakerDetailsScreenUiState

    data class Error(val message: String) : SpeakerDetailsScreenUiState

    data class SpeakerNotFound(val message: String) : SpeakerDetailsScreenUiState
}

@HiltViewModel
class SpeakerDetailsScreenViewModel @Inject constructor(
    private val speakersRepo: SpeakersRepo,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<SpeakerDetailsScreenUiState>(SpeakerDetailsScreenUiState.Loading)
    val uiState = _uiState.asStateFlow()

    suspend fun getSpeakerByName(name: String) {
        when (val result = speakersRepo.getSpeakerByName(name)) {
            is ResourceResult.Success -> {
                val data = result.data
                if (data == null) {
                    _uiState.value = SpeakerDetailsScreenUiState.SpeakerNotFound(message = "Speaker Not found")
                } else {
                    val speaker = SpeakerUI(
                        id = 1,
                        imageUrl = data.avatar,
                        name = data.name,
                        tagline = data.tagline,
                        bio = data.biography,
                        twitterHandle = data.twitter
                    )
                    _uiState.value = SpeakerDetailsScreenUiState.Success(speaker = speaker)
                }
            }
            is ResourceResult.Error -> {
                _uiState.value = SpeakerDetailsScreenUiState.Error(message = result.message)
            }
            else -> {}
        }
    }
}