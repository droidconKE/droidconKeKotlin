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

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ke.droidcon.kotlin.datasource.local.Database
import ke.droidcon.kotlin.datasource.local.model.UpdateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UpdatesDaoTest {
    private lateinit var db: Database
    private lateinit var updatesDao: UpdatesDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), Database::class.java).allowMainThreadQueries().build()
        updatesDao = db.updatesDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `updates come newest first, a tie going to the row written last`() =
        runTest {
            updatesDao.upsert(listOf(update("old", at = 1), update("first", at = 2), update("second", at = 2)))

            assertEquals(listOf("second", "first", "old"), updatesDao.updates().first().map { it.title })
        }

    @Test
    fun `trimming keeps the newest rows and drops the rest`() =
        runTest {
            updatesDao.upsert((1L..5L).map { update("update $it", at = it) })

            updatesDao.trim(limit = 3)

            assertEquals(listOf("update 5", "update 4", "update 3"), updatesDao.updates().first().map { it.title })
        }

    private fun update(
        title: String,
        at: Long,
    ) = UpdateEntity(kind = "Added", sessionId = null, title = title, count = 0, createdAt = at)
}