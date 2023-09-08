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

import com.android254.data.repos.local.LocalSpeakersDataSource
import com.android254.data.repos.remote.RemoteSpeakersDataSource
import com.android254.domain.models.ResourceResult
import com.android254.domain.models.Speaker
import com.android254.domain.repos.SpeakersRepo
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class SpeakersManager @Inject constructor(
    private val localSpeakersDataSource: LocalSpeakersDataSource,
    private val remoteSpeakersDataSource: RemoteSpeakersDataSource

) : SpeakersRepo {
    // the repo is offline first i.e the data should only be loaded from the local data source

    override fun fetchSpeakers(): Flow<List<Speaker>> = localSpeakersDataSource.getCachedSpeakers()

    override suspend fun fetchSpeakerCount(): Flow<Int> =
        localSpeakersDataSource.fetchCachedSpeakerCount()

    override suspend fun getSpeakerById(id: Int): ResourceResult<Speaker> =
        ResourceResult.Success(localSpeakersDataSource.getCachedSpeakerById(id))

    override suspend fun syncSpeakers() {
        val response = remoteSpeakersDataSource.getAllSpeakersRemote()
        when (response) {
            is ResourceResult.Success -> {
                localSpeakersDataSource.deleteAllCachedSpeakers()
                localSpeakersDataSource.saveCachedSpeakers(
                    speakers = response.data ?: emptyList()
                )
                Timber.d("Sync speakers successful")
            }

            is ResourceResult.Error -> {
                Timber.d("Sync speakers failed ${response.message}")
            }

            else -> {
            }
        }
    }
}