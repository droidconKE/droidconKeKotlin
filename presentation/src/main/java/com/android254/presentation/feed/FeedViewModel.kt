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
package com.android254.presentation.feed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.models.ResourceResult
import com.android254.domain.repos.FeedRepo
import com.android254.presentation.feed.view.FeedUIState
import com.android254.presentation.feed.view.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepo: FeedRepo
) : ViewModel() {
    var viewState: FeedUIState by mutableStateOf(FeedUIState.Loading)
        private set

    fun fetchFeed() {
        viewModelScope.launch {
            viewState = when (val value = feedRepo.fetchFeed()) {
                is ResourceResult.Empty -> FeedUIState.Empty
                is ResourceResult.Error -> FeedUIState.Error(value.message)
                is ResourceResult.Loading -> FeedUIState.Loading
                is ResourceResult.Success -> FeedUIState.Success(value.data?.map { it.toPresentation() }
                    ?: emptyList())
            }
        }
    }
}