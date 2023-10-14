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
import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import com.android254.domain.repos.HomeRepo
import com.android254.domain.work.SyncDataWorkManager
import com.android254.presentation.home.viewstate.HomeViewState
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.sessions.mappers.getTimePeriod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepo: HomeRepo,
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
                sponsors = it.sponsors.map { it.sponsorLogoUrl },
                organizedBy = it.organizers.map { it.organizerLogoUrl },
                sessions = it.sessions.toSessionsPresentation()
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

    private fun List<Session>.toSessionsPresentation() =
        map {
            val startTime = getTimePeriod(it.startDateTime)
            val gson = Gson()
            val typeToken = object : TypeToken<List<SpeakerUI>>() {}.type
            val speakers = gson.fromJson<List<SpeakerUI>>(it.speakers, typeToken)
            val hasNoSpeakers = speakers.isEmpty()

            SessionPresentationModel(
                id = it.id,
                title = it.title,
                description = it.description,
                venue = it.rooms,
                speakerImage = if (hasNoSpeakers) "" else speakers.first().imageUrl.toString(),
                speakerName = if (hasNoSpeakers) "" else speakers.first().name,
                startTime = startTime.time,
                endTime = it.endTime,
                amOrPm = startTime.period,
                isStarred = false,
                level = it.sessionLevel,
                format = it.sessionFormat,
                startDate = it.startDateTime,
                endDate = it.endDateTime,
                remoteId = it.remote_id,
                isService = it.isServiceSession,
                sessionImage = it.sessionImage ?: ""
            )
        }
}