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
package com.android254.presentation.activity

import com.android254.domain.models.Session
import com.android254.domain.repos.SessionsRepo
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val sessionsRepo = mockk<SessionsRepo>()
    private val clock = mockk<Clock>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { clock.now() } returns Instant.fromEpochMilliseconds(0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should fetch current and up next sessions`() =
        runTest {
            val session = createSession(id = "1", title = "Title")
            val currentSessions = listOf(session)
            val upNextSessions = listOf(session)

            coEvery { sessionsRepo.fetchCurrentSessions(any()) } returns flowOf(currentSessions)
            coEvery { sessionsRepo.fetchUpNextSessions(any()) } returns flowOf(upNextSessions)

            val viewModel = MainViewModel(sessionsRepo, clock)

            val job = launch { viewModel.sessionState.collect() }
            runCurrent()

            assertThat(viewModel.sessionState.value.current.size, `is`(1))
            assertThat(viewModel.sessionState.value.current[0].title, `is`("Title"))
            assertThat(viewModel.sessionState.value.upNext.size, `is`(1))
            assertThat(viewModel.sessionState.value.upNext[0].title, `is`("Title"))
            job.cancel()
        }

    @Test
    fun `should refresh sessions after ticker interval`() =
        runTest {
            val session1 = createSession(id = "1", title = "Title 1")
            val session2 = createSession(id = "2", title = "Title 2")

            coEvery { sessionsRepo.fetchCurrentSessions(any()) } returnsMany
                listOf(
                    flowOf(listOf(session1)),
                    flowOf(listOf(session2)),
                )
            coEvery { sessionsRepo.fetchUpNextSessions(any()) } returns flowOf(emptyList())

            val viewModel = MainViewModel(sessionsRepo, clock)

            val job = launch { viewModel.sessionState.collect() }
            runCurrent()

            assertThat(viewModel.sessionState.value.current[0].title, `is`("Title 1"))

            // Advance time by 1 minute to trigger ticker
            advanceTimeBy(60001)
            runCurrent()

            assertThat(viewModel.sessionState.value.current[0].title, `is`("Title 2"))
            job.cancel()
        }

    private fun createSession(
        id: String,
        title: String,
    ) =
        Session(
            id = id,
            description = "Description",
            sessionFormat = "Format",
            sessionLevel = "Level",
            slug = "slug-$id",
            title = title,
            endDateTime = "2023-11-17 10:00:00",
            endTime = "10:00 AM",
            isBookmarked = false,
            isKeynote = false,
            isServiceSession = false,
            sessionImage = "",
            startDateTime = "2023-11-17 09:00:00",
            startTime = "09:00 AM",
            rooms = "Room 1",
            speakers = listOf(),
            remoteId = id,
            eventDay = "1",
        )
}