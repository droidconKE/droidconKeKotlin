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
package com.android254.presentation.home.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.flags.FeatureFlags
import com.android254.domain.repos.SessionsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import ke.droidcon.kotlin.core.common.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StarredSummaryViewModel
    @Inject
    constructor(
        sessionsRepo: SessionsRepo,
        private val featureFlags: FeatureFlags,
        private val generator: StarredSummaryGenerator,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        val state: StateFlow<StarredSummaryState> =
            flow {
                if (!featureFlags.isStarredSummaryEnabled()) {
                    emit(StarredSummaryState.Hidden)
                    return@flow
                }
                emit(StarredSummaryState.Loading)
                var lastIds: List<String>? = null
                sessionsRepo.fetchSessions().collect { sessions ->
                    val starred = sessions.filter { it.isBookmarked }
                    val ids = starred.map { it.id }.sorted()
                    if (ids == lastIds) return@collect
                    lastIds = ids
                    if (starred.isEmpty()) {
                        emit(StarredSummaryState.NoStarred)
                        return@collect
                    }
                    emit(StarredSummaryState.Loading)
                    emit(
                        when (val result = generator.summarise(starred)) {
                            is SummaryResult.Success -> StarredSummaryState.Ready(result.summary)
                            SummaryResult.Unavailable -> StarredSummaryState.Unavailable
                            SummaryResult.Failed -> StarredSummaryState.Failed
                        },
                    )
                }
            }.flowOn(ioDispatcher)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), StarredSummaryState.Hidden)
    }