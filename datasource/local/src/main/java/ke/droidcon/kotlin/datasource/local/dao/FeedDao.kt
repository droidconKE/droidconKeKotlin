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
package ke.droidcon.kotlin.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import ke.droidcon.kotlin.datasource.local.model.FeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao : BaseDao<FeedEntity> {
    @Query("SELECT * FROM feed")
    fun fetchFeed(): Flow<List<FeedEntity>>

    @Query("SELECT * FROM feed WHERE id =:id")
    fun fetchFeedById(id: Int): Flow<FeedEntity?>

    @Query("DELETE FROM feed")
    suspend fun deleteAllFeed()
}