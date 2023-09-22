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
package ke.droidcon.kotlin.datasource.local.source

import androidx.sqlite.db.SimpleSQLiteQuery
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.local.dao.SessionDao
import ke.droidcon.kotlin.datasource.local.di.LocalSourceIoDispatcher
import ke.droidcon.kotlin.datasource.local.model.SessionEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LocalSessionsDataSourceImpl @Inject constructor(
    private val sessionDao: SessionDao,
    @LocalSourceIoDispatcher private val localSourceIoDispatcher: CoroutineDispatcher
) : LocalSessionsDataSource {
    override fun getCachedSessions(): Flow<List<SessionEntity>> {
        return sessionDao.fetchSessions()
            .flowOn(localSourceIoDispatcher)
    }

    override suspend fun saveCachedSessions(sessions: List<SessionEntity>) {
        withContext(localSourceIoDispatcher) {
            sessionDao.insert(items = sessions)
        }
    }

    override suspend fun deleteCachedSessions() {
        withContext(localSourceIoDispatcher) {
            sessionDao.clearSessions()
        }
    }

    override fun getCachedSessionById(id: String): Flow<SessionEntity?> =
        sessionDao.getSessionById(id = id).flowOn(localSourceIoDispatcher)

    override fun fetchSessionWithFilters(query: String): Flow<List<SessionEntity>> =
        sessionDao.fetchSessionsWithFilters(SimpleSQLiteQuery(query))
            .flowOn(localSourceIoDispatcher)

    override suspend fun updateBookmarkedStatus(id: String, isBookmarked: Boolean) {
        withContext(localSourceIoDispatcher) {
            sessionDao.updateBookmarkedStatus(id = id, isBookmarked = isBookmarked)
        }
    }

    override suspend fun getBookmarkStatus(id: String): Boolean {
        return withContext(localSourceIoDispatcher) {
            sessionDao.getBookmarkStatus(id = id)
        }
    }
}