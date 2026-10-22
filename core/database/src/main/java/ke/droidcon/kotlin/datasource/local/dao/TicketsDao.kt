/*
 * Copyright 2026 DroidconKE
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
import androidx.room.Transaction
import ke.droidcon.kotlin.datasource.local.model.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketsDao : BaseDao<TicketEntity> {
    @Query("SELECT * FROM tickets LIMIT 1")
    fun ticket(): Flow<TicketEntity?>

    @Query("SELECT * FROM tickets LIMIT 1")
    suspend fun current(): TicketEntity?

    @Transaction
    suspend fun replace(ticket: TicketEntity) {
        clear()
        upsert(ticket)
    }

    /** A no-op once the row is gone, so a refresh cannot bring back a removed ticket. */
    @Query("UPDATE tickets SET status = :status, checkIns = :checkIns WHERE number = :number")
    suspend fun updateStatus(
        number: String,
        status: String,
        checkIns: String,
    )

    @Query("DELETE FROM tickets")
    suspend fun clear()
}