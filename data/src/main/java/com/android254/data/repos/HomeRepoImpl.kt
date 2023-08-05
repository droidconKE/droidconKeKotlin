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

import com.android254.data.di.IoDispatcher
import com.android254.data.network.apis.SponsorsApi
import com.android254.data.network.models.responses.SponsorsPagedResponse
import com.android254.data.repos.local.LocalSessionsDataSource
import com.android254.data.repos.local.LocalSpeakersDataSource
import com.android254.data.repos.local.LocalSponsorsDataSource
import com.android254.data.repos.mappers.toDomain
import com.android254.domain.models.DataResult
import com.android254.domain.models.HomeDetails
import com.android254.domain.models.ResourceResult
import com.android254.domain.models.Session
import com.android254.domain.repos.HomeRepo
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.SpeakersRepo
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class HomeRepoImpl @Inject constructor(
    private val localSpeakersDataSource: LocalSpeakersDataSource,
    private val localSessionsDataSource: LocalSessionsDataSource,
    private val localSponsorsDataSource: LocalSponsorsDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HomeRepo {

    override fun fetchHomeDetails(): Flow<HomeDetails> {
        val sponsorsflow = localSponsorsDataSource.fetchCachedSponsors()
        val speakersflow = localSpeakersDataSource.getCachedSpeakers()
        val sessionsflow = localSessionsDataSource.getCachedSessions()
        return combine(sponsorsflow,speakersflow,sessionsflow){ sponsors, speakers, sessions ->
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
                organizers = listOf()
            )
        }
    }

    private fun getSessionsFromResourceResult(result: ResourceResult<List<Session>>): List<Session> {
        if (result is ResourceResult.Success) {
            return result.data ?: emptyList()
        }

        return emptyList()
    }

    private fun DataResult<SponsorsPagedResponse>.getSponsorsList() =
        when (this) {
            is DataResult.Success -> this.data.data.map { it.toDomain() }
            else -> emptyList()
        }
}