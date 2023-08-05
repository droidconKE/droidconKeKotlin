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

import android.os.Build
import androidx.annotation.RequiresApi
import com.android254.data.di.IoDispatcher
import com.android254.data.network.apis.SessionsApi
import com.android254.data.network.models.responses.SessionDTO
import com.android254.domain.models.ResourceResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

interface RemoteSessionsDataSource {

    suspend fun getAllSessionsRemote(): ResourceResult<List<SessionDTO>>
}

class RemoteSessionsDataSourceImpl(
    private val api: SessionsApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RemoteSessionsDataSource {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getAllSessionsRemote(): ResourceResult<List<SessionDTO>> {
        return withContext(ioDispatcher) {
            val response = api.fetchSessions()
            val sessions = response.data.flatMap { (_, value) -> value }
            return@withContext ResourceResult.Success(data = sessions)
        }
    }
}