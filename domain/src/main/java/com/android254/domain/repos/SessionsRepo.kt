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
package com.android254.domain.repos

import com.android254.domain.models.Session
import com.android254.domain.models.SessionsInformationDomainModel
import kotlinx.coroutines.flow.Flow

interface SessionsRepo {
    fun fetchSessions(): Flow<List<Session>>

    suspend fun fetchSessionsInformation(): Flow<SessionsInformationDomainModel>

    fun fetchFilteredSessions(query: String): Flow<List<Session>>

    fun fetchFilteredSessions(vararg filters: List<String>)

    fun fetchBookmarkedSessions(): Flow<List<Session>>

    fun fetchSessionById(id: String): Flow<Session?>

    suspend fun bookmarkSession(id: String)

    suspend fun unBookmarkSession(id: String)

    suspend fun syncSessions()
}