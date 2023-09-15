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
package com.android254.data.repos.local

import com.android254.data.dao.SpeakerDao
import com.android254.data.repos.mappers.toDomainModel
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.Speaker
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.speakers.model.SpeakerDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalSpeakersDataSourceImpl @Inject constructor(
    private val speakerDao: SpeakerDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LocalSpeakersDataSource {

    override fun getCachedSpeakers(): Flow<List<Speaker>> =
        speakerDao.fetchSpeakers()
            .map { speakers -> speakers.map { it.toDomainModel() } }
            .flowOn(ioDispatcher)

    override suspend fun saveCachedSpeakers(speakers: List<SpeakerDTO>) =
        speakerDao.insert(items = speakers.map { it.toEntity() })

    override suspend fun getCachedSpeakerById(speakerId: Int): Speaker? {
        return withContext(ioDispatcher) {
            speakerDao.getSpeakerById(id = speakerId)?.toDomainModel()
        }
    }

    override fun fetchCachedSpeakerCount(): Flow<Int> =
        speakerDao.fetchSpeakerCount().flowOn(ioDispatcher)

    override suspend fun deleteAllCachedSpeakers() {
        withContext(ioDispatcher) {
            speakerDao.deleteAll()
        }
    }
}