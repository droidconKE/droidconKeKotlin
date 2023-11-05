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
package com.android254.presentation.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.models.Speaker
import com.android254.domain.repos.HomeRepo
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.home.viewstate.HomeViewState
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.sessions.mappers.toPresentationModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    homeRepo: HomeRepo,
    private val syncDataWorkManager: SyncDataWorkManager
) : ViewModel() {

    val isSyncing = syncDataWorkManager.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false
        )

    val viewState: StateFlow<HomeViewState> = homeRepo.fetchHomeDetails()
        .map {
            HomeViewState(
                isPosterVisible = it.isEventBannerEnable,
                isCallForSpeakersVisible = it.isCallForSpeakersEnable,
                linkToCallForSpeakers = it.linkToCallForSpeakers,
                isSignedIn = false,
                speakers = it.speakers.toSpeakersPresentation(),
                isSpeakersSectionVisible = it.isSpeakersSessionEnable,
                isSessionsSectionVisible = it.isSessionsSectionEnable,
                sponsors = it.sponsors.map { sponsor -> sponsor.sponsorLogoUrl },
                organizedBy = it.organizers.map { organizer -> organizer.organizerLogoUrl },
                sessions = it.sessions.map { session -> session.toPresentationModel() }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = HomeViewState()
        )

    fun startRefresh() {
        viewModelScope.launch {
            syncDataWorkManager.startSync()
        }
    }

    private fun List<Speaker>.toSpeakersPresentation() =
        map {
            SpeakerUI(
                imageUrl = it.avatar,
                name = it.name,
                tagline = it.tagline,
                bio = it.biography,
                twitterHandle = it.twitter
            )
        }
}