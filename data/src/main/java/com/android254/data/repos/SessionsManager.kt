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

import com.android254.data.repos.mappers.toDomainModel
import com.android254.data.repos.mappers.toEntity
import com.android254.domain.models.Session
import com.android254.domain.models.SessionsInformationDomainModel
import com.android254.domain.repos.SessionsRepo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import ke.droidcon.kotlin.datasource.local.dao.BookmarkDao
import ke.droidcon.kotlin.datasource.local.model.BookmarkEntity
import ke.droidcon.kotlin.datasource.local.source.LocalSessionsDataSource
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import ke.droidcon.kotlin.datasource.remote.sessions.RemoteSessionsDataSource
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class SessionsManager @Inject constructor(
    private val localSessionsDataSource: LocalSessionsDataSource,
    private val remoteSessionsDataSource: RemoteSessionsDataSource,
    private val bookmarkDao: BookmarkDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SessionsRepo {

    override fun fetchSessions(): Flow<List<Session>> {
        val bookmarksFlow = bookmarkDao.getBookmarkIds()
        val sessionsFlow = localSessionsDataSource.getCachedSessions()
        return combine(sessionsFlow, bookmarksFlow) { sessions, bookmarks ->
            sessions
                .map { session ->
                    session.toDomainModel().copy(isBookmarked = bookmarks.map { it.sessionId }.contains(session.id.toString()))
                }
        }.flowOn(ioDispatcher)
    }

    override suspend fun fetchSessionsInformation(): Flow<SessionsInformationDomainModel> = combine(
        localSessionsDataSource.getCachedSessions(),
        bookmarkDao.getBookmarkIds()
    ) { sessions, bookmarks ->
        val eventDays = sessions.groupBy { it.startTimestamp.toEventDay() }.keys.toList()
        SessionsInformationDomainModel(
            sessions = sessions.map { session -> session.toDomainModel().copy(isBookmarked = bookmarks.map { it.sessionId }.contains(session.id.toString())) },
            eventDays = eventDays
        )
    }

    private fun Long.toEventDay(): String {
        val date = Date(this)
        val sdf = SimpleDateFormat("dd", Locale.getDefault())
        return sdf.format(date)
    }

    override fun fetchBookmarkedSessions(): Flow<List<Session>> {
        val bookmarksFlow = bookmarkDao.getBookmarkIds()
        val sessionsFlow = localSessionsDataSource.getCachedSessions()
        return combine(sessionsFlow, bookmarksFlow) { sessions, bookmarks ->
            sessions.map { session ->
                session.toDomainModel().copy(isBookmarked = bookmarks.map { it.sessionId }.contains(session.id.toString()))
            }
                .filter { session -> session.isBookmarked }
        }.flowOn(ioDispatcher)
    }

    override fun fetchFilteredSessions(query: String): Flow<List<Session>> {
        val filteredSessions = localSessionsDataSource.fetchSessionWithFilters(query)
        val bookmarksFlow = bookmarkDao.getBookmarkIds()
        return combine(filteredSessions, bookmarksFlow) { sessions, bookmarks ->
            sessions.map { session ->
                session.toDomainModel().copy(
                    isBookmarked = bookmarks.map { it.sessionId }.contains(session.id.toString())
                )
            }
        }.flowOn(ioDispatcher)
    }

    override fun fetchFilteredSessions(vararg filters: List<String>) {
        //
    }

    override fun fetchSessionById(id: String): Flow<Session?> {
        val bookmarksFlow = bookmarkDao.getBookmarkIds()
        val sessionFlow = localSessionsDataSource.getCachedSessionById(id).map {
            it?.toDomainModel()
        }
        return combine(sessionFlow, bookmarksFlow) { session, bookmarks ->
            session?.copy(isBookmarked = bookmarks.map { it.sessionId }.contains(session.id))
        }.flowOn(ioDispatcher)
    }

    override suspend fun bookmarkSession(id: String) {
        withContext(ioDispatcher) {
            bookmarkDao.insert(BookmarkEntity(id))
        }
    }

    override suspend fun unBookmarkSession(id: String) {
        withContext(ioDispatcher) {
            bookmarkDao.delete(BookmarkEntity(id))
        }
    }

    override suspend fun syncSessions() {
        when (val response = remoteSessionsDataSource.getAllSessionsRemote()) {
            is DataResult.Success -> {
                localSessionsDataSource.deleteCachedSessions()
                localSessionsDataSource.saveCachedSessions(
                    sessions = response.data.map { session -> session.toEntity() }
                )
                Timber.d("Sync sessions successful")
            }

            is DataResult.Error -> {
                Timber.d("Sync sessions failed ${response.message}")
            }

            else -> {
            }
        }
    }
}