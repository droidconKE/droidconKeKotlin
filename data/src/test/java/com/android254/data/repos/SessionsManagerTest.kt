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

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android254.data.dao.BookmarkDao
import com.android254.data.dao.SessionDao
import com.android254.data.db.Database
import com.android254.data.db.model.SessionEntity
import com.android254.data.network.apis.SessionsApi
import com.android254.data.repos.local.LocalSessionsDataSource
import com.android254.data.repos.mappers.toDomainModel
import com.android254.data.repos.remote.RemoteSessionsDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SessionsManagerTest {
    private val mockLocalSessionsDataSource = mockk<LocalSessionsDataSource>()
    private val mockRemoteSessionsDataSource = mockk<RemoteSessionsDataSource>()
    private lateinit var bookmarkDao: BookmarkDao
    private lateinit var database: Database
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    @Before
    fun beforeTest() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            Database::class.java
        ).allowMainThreadQueries().build()

        bookmarkDao = database.bookmarkDao()
    }

    @Test
    fun `test it fetches from the local cache`() = runTest {
       coEvery { mockLocalSessionsDataSource.getCachedSessions() } returns
               flowOf(listOf(sessionEntity.toDomainModel()))
        val sessions = mockLocalSessionsDataSource.getCachedSessions().first()
        assert(sessions[0].description == sessionEntity.description)
        coVerify (atLeast = 1){
            mockLocalSessionsDataSource.getCachedSessions()
        }
    }

    @After
    fun afterTest() {
        database.close()
    }
}
val sessionEntity = SessionEntity(
    id = 1,
    remote_id = "1234567890",
    description = "This is a keynote session about the future of technology.",
    sessionFormat = "Keynote",
    sessionLevel = "Beginner",
    slug = "keynote-session",
    title = "The Future of Technology",
    endDateTime = "2023-08-17T12:00:00Z",
    endTime = "12:00 PM",
    isBookmarked = false,
    isKeynote = true,
    isServiceSession = false,
    sessionImage = "https://example.com/session-1.jpg",
    startDateTime = "2023-08-17T10:00:00Z",
    startTime = "10:00 AM",
    rooms = "Room 1",
    speakers = "John Doe, Jane Doe",
    startTimestamp = 1638457600000,
    sessionImageUrl = "https://example.com/session-1.jpg"
)