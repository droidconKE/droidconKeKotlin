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
package ke.droidcon.kotlin.datasource.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import ke.droidcon.kotlin.datasource.local.Database
import ke.droidcon.kotlin.datasource.local.model.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SessionDaoTest {
    private lateinit var sessionDao: SessionDao
    private lateinit var db: Database

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room.inMemoryDatabaseBuilder(
                context,
                Database::class.java,
            )
                .allowMainThreadQueries()
                .build()
        sessionDao = db.sessionDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun `test sessionDao fetches all sessions`() =
        runTest {
            val session = createSession(id = 1, title = "Title", startTimestamp = 0L)
            sessionDao.insert(session)
            val result = sessionDao.fetchSessions().first()
            assertThat(result.size, `is`(1))
            assertThat(result[0].title, `is`("Title"))
        }

    @Test
    fun `test fetchCurrentSessions returns sessions happening now`() =
        runTest {
            val currentTime = 1000L
            val currentSession = createSession(id = 1, title = "Current", startTimestamp = 500L) // Started before, still on
            val futureSession = createSession(id = 2, title = "Future", startTimestamp = 5000L)

            sessionDao.insert(currentSession)
            sessionDao.insert(futureSession)

            val result = sessionDao.fetchCurrentSessions(currentTime).first()
            assertThat(result.size, `is`(1))
            assertThat(result[0].title, `is`("Current"))
        }

    @Test
    fun `test fetchUpNextSessions returns future sessions`() =
        runTest {
            val currentTime = 1000L
            val pastSession = createSession(id = 1, title = "Past", startTimestamp = 500L)
            val nextSession1 = createSession(id = 2, title = "Next 1", startTimestamp = 2000L)
            val nextSession2 = createSession(id = 3, title = "Next 2", startTimestamp = 3000L)

            sessionDao.insert(pastSession)
            sessionDao.insert(nextSession1)
            sessionDao.insert(nextSession2)

            val result = sessionDao.fetchUpNextSessions(currentTime).first()
            assertThat(result.size, `is`(2))
            assertThat(result[0].title, `is`("Next 1"))
            assertThat(result[1].title, `is`("Next 2"))
        }

    @Test
    fun `test fetchUpNextSessions limits to 5 sessions`() =
        runTest {
            val currentTime = 0L
            (1..10).forEach { i ->
                sessionDao.insert(createSession(id = i, title = "Session $i", startTimestamp = i * 1000L))
            }

            val result = sessionDao.fetchUpNextSessions(currentTime).first()
            assertThat(result.size, `is`(5))
        }

    private fun createSession(
        id: Int,
        title: String,
        startTimestamp: Long,
    ) =
        SessionEntity(
            id = id,
            remote_id = id.toString(),
            description = "Description",
            sessionFormat = "Format",
            sessionLevel = "Level",
            slug = "slug-$id",
            title = title,
            endDateTime = "",
            endTime = "",
            isBookmarked = false,
            isKeynote = false,
            isServiceSession = false,
            sessionImage = "",
            startDateTime = "",
            startTime = "",
            rooms = "",
            speakers = "",
            startTimestamp = startTimestamp,
            sessionImageUrl = "",
        )
}