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
package com.android254.data.repos

import com.android254.domain.models.ScheduleUpdate
import ke.droidcon.kotlin.datasource.local.model.UpdateEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Instant

class UpdatesManagerTest {
    @Test
    fun `a stored update reads back with its id and time`() {
        val row = UpdateEntity(id = 7, kind = "Moved", sessionId = "talk", title = "Compose", count = 0, createdAt = 1_000)

        assertEquals(ScheduleUpdate(7, ScheduleUpdate.Kind.Moved, "talk", "Compose", 0, Instant.fromEpochMilliseconds(1_000)), row.toDomain())
    }

    @Test
    fun `a kind written by a newer version is skipped`() {
        assertNull(UpdateEntity(kind = "SomethingNewer", sessionId = null, title = "", count = 0, createdAt = 1_000).toDomain())
    }
}