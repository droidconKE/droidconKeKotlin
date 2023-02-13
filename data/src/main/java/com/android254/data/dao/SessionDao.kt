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
package com.android254.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.android254.data.db.model.SessionEntity

@Dao
interface SessionDao : BaseDao<SessionEntity> {
    @Query("SELECT * FROM sessions ORDER BY startTimestamp ASC")
    fun fetchSessions(): List<SessionEntity>

    @Query("DELETE FROM sessions")
    fun clearSessions()

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getSessionById(id: String): SessionEntity?

    @RawQuery
    fun fetchSessionsWithFilters(query: SupportSQLiteQuery): List<SessionEntity>

    @Query("UPDATE sessions SET isBookmarked = :isBookmarked WHERE remote_id = :id")
    fun updateBookmarkedStatus(id: String, isBookmarked: Boolean)

    @Query("SELECT isBookmarked FROM sessions WHERE remote_id = :id")
    fun getBookmarkStatus(id: String): Boolean
}