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
package ke.droidcon.kotlin.datasource.remote.sessions

import android.os.Build
import androidx.annotation.RequiresApi
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.sessions.model.SessionDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RemoteSessionsDataSourceImpl(
    private val api: SessionsApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RemoteSessionsDataSource {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getAllSessionsRemote(): DataResult<List<SessionDTO>> {
        return withContext(ioDispatcher) {
            val response = api.fetchSessions()
            val sessions = response.data.flatMap { (_, value) -> value }
            return@withContext DataResult.Success(data = sessions)
        }
    }
}