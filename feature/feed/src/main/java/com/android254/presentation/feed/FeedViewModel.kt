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
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.repos.FeedRepo
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.UpdatesRepo
import com.android254.presentation.feed.view.FeedUIState
import com.android254.presentation.feed.view.updateItems
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.core.common.di.ConferenceTimeZone
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
class FeedViewModel
    @Inject
    constructor(
        feedRepo: FeedRepo,
        updatesRepo: UpdatesRepo,
        sessionsRepo: SessionsRepo,
        clock: Clock,
        @ConferenceTimeZone conferenceTimeZone: TimeZone,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        val uiState: StateFlow<FeedUIState> =
            combine(feedRepo.fetchFeed(), updatesRepo.scheduleUpdates(), sessionsRepo.fetchSessions()) { posts, updates, sessions ->
                val items = updateItems(posts, updates, sessions, clock.now(), conferenceTimeZone)
                if (items.isEmpty()) FeedUIState.Empty else FeedUIState.Success(items.toImmutableList())
            }.onStart { emit(FeedUIState.Loading) }
                .catch { emit(FeedUIState.Error(message = "An unexpected error occurred")) }
                .flowOn(ioDispatcher)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue = FeedUIState.Empty,
                )
    }