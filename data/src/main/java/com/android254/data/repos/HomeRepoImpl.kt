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
package com.android254.data.repos

import com.android254.domain.models.HomeDetails
import com.android254.domain.models.OrganizingPartners
import com.android254.domain.repos.HomeRepo
import com.android254.domain.repos.OrganizersRepo
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.SpeakersRepo
import com.android254.domain.repos.SponsorsRepo
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class HomeRepoImpl @Inject constructor(
    private val speakersRepo: SpeakersRepo,
    private val sessionsRepo: SessionsRepo,
    private val sponsorsRepo: SponsorsRepo,
    private val organizersRepo: OrganizersRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HomeRepo {

    override fun fetchHomeDetails(): Flow<HomeDetails> {
        val sponsorsflow = sponsorsRepo.getAllSponsors()
        val speakersflow = speakersRepo.fetchSpeakers()
        val sessionsflow = sessionsRepo.fetchSessions()
        val organizerflow = organizersRepo.getOrganizers()
        return combine(sponsorsflow, speakersflow, sessionsflow, organizerflow) { sponsors, speakers, sessions, organizers ->
            HomeDetails(
                isCallForSpeakersEnable = true,
                linkToCallForSpeakers = "https://t.co/lEQQ9VZQr4",
                isEventBannerEnable = true,
                speakers = speakers,
                speakersCount = speakers.size,
                isSpeakersSessionEnable = speakers.isNotEmpty(),
                sessions = sessions,
                sessionsCount = sessions.size,
                isSessionsSectionEnable = sessions.isNotEmpty(),
                sponsors = sponsors,
                organizers = organizers.map {
                    OrganizingPartners(organizerName = it.name, organizerLogoUrl = it.photo)
                }
            )
        }
    }
}