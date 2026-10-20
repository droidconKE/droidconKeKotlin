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
package ke.droidcon.kotlin.datasource.local.source

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ke.droidcon.kotlin.datasource.local.Database
import ke.droidcon.kotlin.datasource.local.model.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class LocalSessionsDataSourceTest {
    private lateinit var db: Database
    private lateinit var dataSource: LocalSessionsDataSource

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), Database::class.java).allowMainThreadQueries().build()
        dataSource = LocalSessionsDataSourceImpl(db.sessionDao(), UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `a room filter finds sessions held in several rooms, and a quote in a name is only text`() =
        runTest {
            dataSource.saveCachedSessions(
                listOf(session("both", rooms = "Opal,Ruby's Lounge"), session("lounge", rooms = "Ruby's Lounge"), session("opal", rooms = "Opal")),
            )

            val inLounge = dataSource.fetchSessionWithFilters(rooms = listOf("Ruby's Lounge"), levels = emptyList(), formats = emptyList()).first()

            assertEquals(setOf("both", "lounge"), inLounge.map { it.remote_id }.toSet())
        }

    @Test
    fun `filters of different kinds must all match, values of one kind any`() =
        runTest {
            dataSource.saveCachedSessions(
                listOf(
                    session("beginner talk", level = "Beginner", format = "Talk"),
                    session("expert talk", level = "Expert", format = "Talk"),
                    session("beginner workshop", level = "Beginner", format = "Workshop"),
                    session("expert workshop", level = "Expert", format = "Workshop"),
                ),
            )

            val talks = dataSource.fetchSessionWithFilters(rooms = emptyList(), levels = listOf("Beginner", "Expert"), formats = listOf("Talk")).first()

            assertEquals(setOf("beginner talk", "expert talk"), talks.map { it.remote_id }.toSet())
        }

    private fun session(
        id: String,
        rooms: String = "Opal",
        level: String = "Beginner",
        format: String = "Talk",
    ) = SessionEntity(
        remote_id = id,
        description = "",
        sessionFormat = format,
        sessionLevel = level,
        slug = id,
        title = id,
        endDateTime = "",
        endTime = "",
        isBookmarked = false,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = "",
        startDateTime = "",
        startTime = "",
        rooms = rooms,
        speakers = "[]",
        startTimestamp = 0,
        endTimeStamp = 0,
        sessionImageUrl = "",
    )
}