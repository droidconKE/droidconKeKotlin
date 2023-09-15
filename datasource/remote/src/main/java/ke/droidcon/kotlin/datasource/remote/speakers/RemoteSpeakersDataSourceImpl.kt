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
package ke.droidcon.kotlin.datasource.remote.speakers

import javax.inject.Inject
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.speakers.model.SpeakerDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RemoteSpeakersDataSourceImpl @Inject constructor(
    private val api: SpeakersApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RemoteSpeakersDataSource {

    override suspend fun getAllSpeakersRemote(): DataResult<List<SpeakerDTO>> {
        return withContext(ioDispatcher) {
            when (val response = api.fetchSpeakers()) {
                is DataResult.Success -> {
                    val speakers = response.data.data
                    return@withContext DataResult.Success(data = speakers)
                }
                is DataResult.Error -> {
                    return@withContext DataResult.Error(message = response.message)
                }
                is DataResult.Loading -> {
                    return@withContext DataResult.Loading(emptyList())
                }
                is DataResult.Empty -> {
                    return@withContext DataResult.Error(message = "Speakers Information not found")
                }
            }
        }
    }
}