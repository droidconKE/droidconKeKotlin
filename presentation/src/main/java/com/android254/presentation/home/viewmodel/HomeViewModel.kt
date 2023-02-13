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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.models.Session
import com.android254.domain.models.Speaker
import com.android254.domain.repos.HomeRepo
import com.android254.presentation.home.viewstate.HomeViewState
import com.android254.presentation.models.SessionPresentationModel
import com.android254.presentation.models.SpeakerUI
import com.android254.presentation.sessions.mappers.getTimePeriod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepo: HomeRepo
) : ViewModel() {

    var viewState by mutableStateOf(HomeViewState())
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun onGetHomeScreenDetails() {
        viewModelScope.launch {
            with(homeRepo.fetchHomeDetails()) {
                viewState
                viewState = viewState.copy(
                    isPosterVisible = this.isEventBannerEnable,
                    isCallForSpeakersVisible = this.isCallForSpeakersEnable,
                    linkToCallForSpeakers = "",
                    isSignedIn = false,
                    speakers = speakers.toSpeakersPresentation(),
                    sponsors = sponsors.map { it.sponsorLogoUrl },
                    organizedBy = organizers.map { it.organizerLogoUrl },
                    sessions = sessions.toSessionsPresentation()
                )
            }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun List<Session>.toSessionsPresentation() =
        map {
            val startTime = getTimePeriod(it.startDateTime)
            val gson = Gson()
            val typeToken = object : TypeToken<List<SpeakerUI>>() {}.type
            val speakers = gson.fromJson<List<SpeakerUI>>(it.speakers, typeToken)
            val hasNoSpeakers = speakers.isEmpty()

            SessionPresentationModel(
                id = it.id.toString(),
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
                remoteId = it.remote_id
            )
        }
}