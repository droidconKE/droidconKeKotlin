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
package com.android254.data.repos.remote

import com.android254.data.di.IoDispatcher
import com.android254.data.network.apis.SpeakersApi
import com.android254.data.network.models.responses.SpeakerDTO
import com.android254.domain.models.DataResult
import com.android254.domain.models.ResourceResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface RemoteSpeakersDataSource {

    suspend fun getAllSpeakersRemote(): ResourceResult<List<SpeakerDTO>>
}
class RemoteSpeakersDataSourceImpl @Inject constructor(
    private val api: SpeakersApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RemoteSpeakersDataSource {

    override suspend fun getAllSpeakersRemote(): ResourceResult<List<SpeakerDTO>> {
        return withContext(ioDispatcher) {
            val response = api.fetchSpeakers()
            when (response) {
                is DataResult.Success -> {
                    val speakers = response.data.data
                    return@withContext ResourceResult.Success(data = speakers)
                }
                is DataResult.Error -> {
                    return@withContext ResourceResult.Error(message = response.message)
                }
                is DataResult.Loading -> {
                    return@withContext ResourceResult.Loading()
                }
                is DataResult.Empty -> {
                    return@withContext ResourceResult.Error(message = "Speakers Information not found")
                }
            }
        }
    }
}