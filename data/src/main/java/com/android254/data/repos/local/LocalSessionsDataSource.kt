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

import androidx.sqlite.db.SupportSQLiteQuery
import com.android254.data.db.model.SessionEntity
import com.android254.domain.models.Session
import ke.droidcon.kotlin.datasource.remote.sessions.model.SessionDTO
import kotlinx.coroutines.flow.Flow

interface LocalSessionsDataSource {

    fun getCachedSessions(): Flow<List<Session>>

    suspend fun deleteCachedSessions()

    fun getCachedSessionById(id: String): Flow<SessionEntity?>

    fun fetchSessionWithFilters(query: SupportSQLiteQuery): Flow<List<Session>>

    suspend fun updateBookmarkedStatus(id: String, isBookmarked: Boolean)

    suspend fun getBookmarkStatus(id: String): Boolean

    suspend fun saveCachedSessions(sessions: List<SessionDTO>)
}