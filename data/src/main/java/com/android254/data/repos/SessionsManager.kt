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

import androidx.sqlite.db.SimpleSQLiteQuery
import com.android254.data.dao.BookmarkDao
import com.android254.data.dao.SessionDao
import com.android254.data.db.model.BookmarkEntity
import com.android254.data.di.IoDispatcher
import com.android254.data.network.apis.SessionsApi
import com.android254.data.repos.mappers.toDomainModel
import com.android254.domain.models.Session
import com.android254.domain.repos.SessionsRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SessionsManager @Inject constructor(
    private val api: SessionsApi,
    private val dao: SessionDao,
    private val bookmarkDao: BookmarkDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SessionsRepo {

    override fun fetchSessions(): Flow<List<Session>> {
        val bookmarksFlow = bookmarkDao.getBookmarkIds()
        val sessionsFlow = dao.fetchSessions()
        return combine(sessionsFlow, bookmarksFlow) { sessions, bookmarks ->
            sessions.map { it.toDomainModel() }
                .map { session ->
                    session.copy(isBookmarked = bookmarks.map { it.sessionId }.contains(session.id))
                }
        }.flowOn(ioDispatcher)
    }
    override fun fetchBookmarkedSessions(): Flow<List<Session>> {
        val bookmarksFlow = bookmarkDao.getBookmarkIds()
        val sessionsFlow = dao.fetchSessions()
        return combine(sessionsFlow, bookmarksFlow) { sessions, bookmarks ->
            sessions.map { it.toDomainModel() }
                .map { session ->
                    session.copy(isBookmarked = bookmarks.map { it.sessionId }.contains(session.id))
                }
                .filter { session -> session.isBookmarked }
        }.flowOn(ioDispatcher)
    }

    override fun fetchFilteredSessions(query: String): Flow<List<Session>> {
        val filteredSessions = dao.fetchSessionsWithFilters(SimpleSQLiteQuery(query))
        val bookmarksFlow = bookmarkDao.getBookmarkIds()
        return combine(filteredSessions, bookmarksFlow) { sessions, bookmarks ->
            sessions.map { session ->
                session.copy(
                    isBookmarked = bookmarks.map { it.sessionId }.contains(session.id.toString())
                )
            }.map { it.toDomainModel() }
        }.flowOn(ioDispatcher)
    }

    override fun fetchSessionById(sessionId: String): Flow<Session?> {
        val bookmarksFlow = bookmarkDao.getBookmarkIds()
        val sessionFlow = dao.getSessionById(sessionId).map {
            it?.toDomainModel()
        }
        return combine(sessionFlow, bookmarksFlow) { session, bookmarks ->
            session?.copy(isBookmarked = bookmarks.map { it.sessionId }.contains(session?.id.toString()))
        }
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
//    @RequiresApi(Build.VERSION_CODES.O)
//    override suspend fun fetchAndSaveSessions(
//        fetchFromRemote: Boolean,
//        query: String?
//    ): ResourceResult<List<Session>> = withContext(ioDispatcher) {
//        val sessions = if (query == null) {
//            dao.fetchSessions().first()
//        } else {
//            dao.fetchSessionsWithFilters(SimpleSQLiteQuery(query))
//        }
//
//        val isDbEmpty = sessions.isEmpty()
//        val hasAQuery = query != null
//        val shouldLoadFromCache = (!isDbEmpty && !fetchFromRemote) || hasAQuery
//        if (shouldLoadFromCache) {
//            return@withContext try {
//                val response = api.fetchSessions()
//                val remoteSessions = response.data.flatMap { (_, value) -> value }
//                if (remoteSessions.isEmpty()) {
//                    ResourceResult.Empty("No sessions just yet")
//                } else {
//                    remoteSessions.let {
//                        dao.clearSessions()
//                        val bookmarkIds = bookmarkDao.getBookmarkIds().map { sessionEntity ->
//                            sessionEntity.sessionId
//                        }
//                        val sessionEntities = it.map { session ->
//                            val newSession = session.toEntity().copy(
//                                isBookmarked = bookmarkIds.contains(session.id)
//                            )
//                            newSession
//                        }
//                        dao.insert(sessionEntities)
//                        ResourceResult.Success(
//                            data = sessionEntities.map { sessionEntity -> sessionEntity.toDomainModel() }
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                when (e) {
//                    is NetworkError -> ResourceResult.Error("Network error")
//                    else -> ResourceResult.Error("Error fetching sessions")
//                }
//            }
//        } else {
//            return@withContext ResourceResult.Success(
//                data = sessions.map {
//                    it.toDomainModel()
//                }
//            )
//        }
//    }
//
// //    override suspend fun fetchSessionById(id: String): ResourceResult<Session> =
// //        withContext(ioDispatcher) {
// //            val session = dao.getSessionById(id)
// //                ?: return@withContext ResourceResult.Error(message = "requested event no longer available")
// //            return@withContext ResourceResult.Success(data = session.toDomainModel())
// //        }
//
//    override suspend fun toggleBookmarkStatus(
//        id: String,
//        isCurrentlyStarred: Boolean
//    ): ResourceResult<Boolean> = withContext(ioDispatcher) {
//        try {
//            dao.updateBookmarkedStatus(id, !isCurrentlyStarred)
//            if (isCurrentlyStarred) {
//                bookmarkDao.delete(BookmarkEntity(id))
//            } else {
//                bookmarkDao.insert(BookmarkEntity(id))
//            }
//        } catch (e: Exception) {
//            when (e) {
//                is NetworkError -> {
//                    return@withContext ResourceResult.Error("Network error")
//                }
//                else -> {
//                    return@withContext ResourceResult.Error("Error fetching sessions")
//                }
//            }
//        }
//        return@withContext ResourceResult.Success(data = dao.getBookmarkStatus(id))
//    }
}