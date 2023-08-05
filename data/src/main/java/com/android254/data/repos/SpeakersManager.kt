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

import com.android254.data.db.Database
import com.android254.data.di.IoDispatcher
import com.android254.data.network.apis.SpeakersApi
import com.android254.data.network.models.responses.SpeakersPagedResponse
import com.android254.data.repos.local.LocalSpeakersDataSource
import com.android254.data.repos.mappers.toDomainModel
import com.android254.data.repos.mappers.toEntity
import com.android254.data.repos.remote.RemoteSpeakersDataSource
import com.android254.domain.models.DataResult
import com.android254.domain.models.ResourceResult
import com.android254.domain.models.Speaker
import com.android254.domain.repos.SpeakersRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SpeakersManager @Inject constructor(
    private val localSpeakersDataSource: LocalSpeakersDataSource,

) : SpeakersRepo {
    // the repo is offline first i.e the data should only be loaded from the local data source

    override fun fetchSpeakers(): Flow<List<Speaker>>  {
        return localSpeakersDataSource.getCachedSpeakers()
    }


    override suspend fun fetchSpeakerCount(): Flow<Int> {
        return localSpeakersDataSource.fetchCachedSpeakerCount()
    }


    override suspend fun getSpeakerById(id: Int): ResourceResult<Speaker> =
         ResourceResult.Success(localSpeakersDataSource.getCachedSpeakerById(id))
}