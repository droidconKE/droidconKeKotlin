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
import com.android254.domain.models.ResourceResult
import com.android254.domain.repos.SpeakersRepo
import com.android254.presentation.models.SpeakerUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class SpeakersViewModel @Inject constructor(
    private val speakersRepo: SpeakersRepo
) : ViewModel() {

    val isLoading = MutableStateFlow(false)
    val message = MutableSharedFlow<String>()

    suspend fun getSpeakers(): List<SpeakerUI> {
        isLoading.value = true
        when (val result = speakersRepo.fetchSpeakers()) {
            is ResourceResult.Success -> {
                isLoading.value = false
                return result.data?.map {
                    SpeakerUI(
                        id = 1,
                        imageUrl = it.avatar,
                        name = it.name,
                        tagline = it.tagline,
                        bio = it.biography,
                        twitterHandle = it.twitter
                    )
                } ?: emptyList()
            }
            is ResourceResult.Error -> {
                message.tryEmit(result.message)
            }
            else -> {}
        }
        isLoading.value = false
        return emptyList()
    }

    suspend fun getSpeakerById(id: Int): SpeakerUI {
        isLoading.value = true
        when (val result = speakersRepo.getSpeakerById(id)) {
            is ResourceResult.Success -> {
                val data = result.data
                return if (data == null) {
                    SpeakerUI()
                } else {
                    SpeakerUI(
                        id = 1,
                        imageUrl = data.avatar,
                        name = data.name,
                        tagline = data.tagline,
                        bio = data.biography,
                        twitterHandle = data.twitter
                    )
                }
            }
            is ResourceResult.Error -> {
                message.tryEmit(result.message)
            }
            else -> {}
        }
        return SpeakerUI()
    }
}