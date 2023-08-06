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

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.sqlite.db.SupportSQLiteQuery
import com.android254.data.dao.SessionDao
import com.android254.data.db.model.SessionEntity
import com.android254.data.di.IoDispatcher
import com.android254.data.network.models.responses.SessionDTO
import com.android254.data.repos.mappers.toDomainModel
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.Session
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface LocalSessionsDataSource {

    fun getCachedSessions(): Flow<List<Session>>

    suspend fun deleteCachedSessions()

    suspend fun getCachedSessionById(id: String): SessionEntity?

    suspend fun fetchSessionWithFilters(query: SupportSQLiteQuery): List<Session>

    suspend fun updateBookmarkedStatus(id: String, isBookmarked: Boolean)

    suspend fun getBookmarkStatus(id: String): Boolean

    suspend fun saveCachedSessions(sessions: List<SessionDTO>)
}

class LocalSessionsDataSourceImpl @Inject constructor(
    private val sessionDao: SessionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LocalSessionsDataSource {
    override fun getCachedSessions(): Flow<List<Session>> {
        return sessionDao.fetchSessions()
            .flowOn(ioDispatcher)
            .map { sessions -> sessions.map { it.toDomainModel() } }
    }


    override suspend fun saveCachedSessions(sessions: List<SessionDTO>) {
        withContext(ioDispatcher) {
            sessionDao.insert(items = sessions.map { it.toEntity() })
        }
    }

    override suspend fun deleteCachedSessions() {
        withContext(ioDispatcher) {
            sessionDao.clearSessions()
        }
    }

    override suspend fun getCachedSessionById(id: String): SessionEntity? {
        return withContext(ioDispatcher) {
            sessionDao.getSessionById(id = id)
        }
    }

    override suspend fun fetchSessionWithFilters(query: SupportSQLiteQuery): List<Session> {
        return withContext(ioDispatcher) {
            sessionDao.fetchSessionsWithFilters(query = query).map { it.toDomainModel() }
        }
    }

    override suspend fun updateBookmarkedStatus(id: String, isBookmarked: Boolean) {
        withContext(ioDispatcher) {
            sessionDao.updateBookmarkedStatus(id = id, isBookmarked = isBookmarked)
        }
    }

    override suspend fun getBookmarkStatus(id: String): Boolean {
        return withContext(ioDispatcher) {
            sessionDao.getBookmarkStatus(id = id)
        }
    }
}