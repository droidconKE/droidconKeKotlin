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

import androidx.lifecycle.viewModelScope
import com.android254.domain.models.Session
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.work.SyncDataWorkManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import ke.droidcon.kotlin.core.notifications.NotificationPermission
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferences
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val sessionsRepo = mockk<SessionsRepo>()
    private val clock = mockk<Clock>()
    private val remoteFeatureToggle = mockk<RemoteFeatureToggle>(relaxed = true)
    private val syncDataWorkManager = mockk<SyncDataWorkManager>(relaxed = true)
    private val notificationPermission = mockk<NotificationPermission>()
    private val preferences = MutableStateFlow(NotificationPreferences())
    private val preferencesStore =
        mockk<NotificationPreferencesStore> {
            every { preferences } returns this@MainViewModelTest.preferences
            coEvery { markPermissionPrompted() } answers {
                this@MainViewModelTest.preferences.value =
                    this@MainViewModelTest.preferences.value.copy(permissionPrompted = true)
            }
        }
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { clock.now() } returns Instant.fromEpochMilliseconds(0)
        // The init block reads cached sessions and kicks off config fetch plus sync.
        coEvery { sessionsRepo.fetchSessions() } returns flowOf(emptyList())
        coEvery { remoteFeatureToggle.syncNowIfEmpty() } returns false
        every { notificationPermission.needsRuntimeRequest() } returns true
        every { notificationPermission.areNotificationsEnabled() } returns false
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

            val viewModel = viewModel()

            val job = launch { viewModel.sessionState.collect() }
            runCurrent()

            assertThat(viewModel.sessionState.value.current.size, `is`(1))
            assertThat(
                viewModel.sessionState.value.current[0]
                    .title,
                `is`("Title"),
            )
            assertThat(viewModel.sessionState.value.upNext.size, `is`(1))
            assertThat(
                viewModel.sessionState.value.upNext[0]
                    .title,
                `is`("Title"),
            )
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

            val viewModel = viewModel()

            val job = launch { viewModel.sessionState.collect() }
            runCurrent()

            assertThat(
                viewModel.sessionState.value.current[0]
                    .title,
                `is`("Title 1"),
            )

            // Advance time by 1 minute to trigger ticker
            advanceTimeBy(60001)
            runCurrent()

            assertThat(
                viewModel.sessionState.value.current[0]
                    .title,
                `is`("Title 2"),
            )
            job.cancel()
        }

    @Test
    fun `starring a session without permission asks once`() =
        runTest {
            val sessions = MutableStateFlow(emptyList<Session>())
            val requests = collectRequests(sessions)

            sessions.value = listOf(createSession(id = "1", title = "A", isBookmarked = true))
            runCurrent()

            assertEquals(1, requests.size)
        }

    @Test
    fun `existing stars at startup do not prompt`() =
        runTest {
            val sessions = MutableStateFlow(starred(3))
            val requests = collectRequests(sessions)

            runCurrent()

            assertEquals(0, requests.size)
        }

    @Test
    fun `a star after existing stars at startup prompts`() =
        runTest {
            val sessions = MutableStateFlow(starred(3))
            val requests = collectRequests(sessions)

            sessions.value = starred(4)
            runCurrent()

            assertEquals(1, requests.size)
        }

    @Test
    fun `a second new star does not prompt again`() =
        runTest {
            val sessions = MutableStateFlow(emptyList<Session>())
            val requests = collectRequests(sessions)

            sessions.value = starred(1)
            runCurrent()
            viewModel.onNotificationPermissionPrompted()
            runCurrent()
            sessions.value = starred(2)
            runCurrent()

            assertEquals(1, requests.size)
            coVerify(exactly = 1) { preferencesStore.markPermissionPrompted() }
        }

    @Test
    fun `leaving the screen while the prompt is being recorded still records it`() =
        runTest {
            val saved = CompletableDeferred<Unit>()
            coEvery { preferencesStore.markPermissionPrompted() } coAnswers {
                saved.await()
                preferences.value = preferences.value.copy(permissionPrompted = true)
            }
            val viewModel = viewModel()

            viewModel.onNotificationPermissionPrompted()
            viewModel.viewModelScope.cancel()
            saved.complete(Unit)
            runCurrent()

            assertEquals(true, preferences.value.permissionPrompted)
        }

    @Test
    fun `a second star before the first prompt is handled does not queue another`() =
        runTest {
            val sessions = MutableStateFlow(emptyList<Session>())
            val requests = collectRequests(sessions)

            sessions.value = starred(1)
            runCurrent()
            sessions.value = starred(2)
            runCurrent()

            assertEquals(1, requests.size)
        }

    @Test
    fun `unstarring does not prompt`() =
        runTest {
            val sessions = MutableStateFlow(starred(2))
            val requests = collectRequests(sessions)

            sessions.value = starred(1)
            runCurrent()

            assertEquals(0, requests.size)
        }

    @Test
    fun `no prompt when permission is already granted`() =
        runTest {
            every { notificationPermission.needsRuntimeRequest() } returns false
            val sessions = MutableStateFlow(emptyList<Session>())
            val requests = collectRequests(sessions)

            sessions.value = starred(1)
            runCurrent()

            assertEquals(0, requests.size)
        }

    @Test
    fun `no prompt once a previous launch asked`() =
        runTest {
            preferences.value = NotificationPreferences(permissionPrompted = true)
            val sessions = MutableStateFlow(emptyList<Session>())
            val requests = collectRequests(sessions)

            sessions.value = starred(1)
            runCurrent()

            assertEquals(0, requests.size)
        }

    @Test
    fun `starring a session that has already started does not prompt`() =
        runTest {
            val sessions = MutableStateFlow(emptyList<Session>())
            val requests = collectRequests(sessions)

            sessions.value = listOf(createSession(id = "1", title = "A", isBookmarked = true, startsAt = -1))
            runCurrent()

            assertEquals(0, requests.size)
        }

    @Test
    fun `starring while notifications are blocked after a previous prompt points at settings`() =
        runTest {
            preferences.value = NotificationPreferences(permissionPrompted = true)
            val sessions = MutableStateFlow(emptyList<Session>())
            val events = collectRemindersOff(sessions)

            sessions.value = starred(1)
            runCurrent()

            assertEquals(1, events.size)
        }

    @Test
    fun `starring a session that has already started does not point at settings`() =
        runTest {
            preferences.value = NotificationPreferences(permissionPrompted = true)
            val sessions = MutableStateFlow(emptyList<Session>())
            val events = collectRemindersOff(sessions)

            sessions.value = listOf(createSession(id = "1", title = "A", isBookmarked = true, startsAt = -1))
            runCurrent()

            assertEquals(0, events.size)
        }

    @Test
    fun `starring while notifications are allowed says nothing`() =
        runTest {
            every { notificationPermission.areNotificationsEnabled() } returns true
            every { notificationPermission.needsRuntimeRequest() } returns false
            val sessions = MutableStateFlow(emptyList<Session>())
            val events = collectRemindersOff(sessions)

            sessions.value = starred(1)
            runCurrent()

            assertEquals(0, events.size)
        }

    @Test
    fun `declining the system prompt points at settings`() =
        runTest {
            val events = collectRemindersOff(MutableStateFlow(emptyList()))

            viewModel.onNotificationPermissionResult(granted = false)
            runCurrent()

            assertEquals(1, events.size)
        }

    @Test
    fun `allowing the system prompt says nothing`() =
        runTest {
            val events = collectRemindersOff(MutableStateFlow(emptyList()))

            viewModel.onNotificationPermissionResult(granted = true)
            runCurrent()

            assertEquals(0, events.size)
        }

    private lateinit var viewModel: MainViewModel

    private fun kotlinx.coroutines.test.TestScope.collectRemindersOff(sessions: MutableStateFlow<List<Session>>): List<Unit> {
        coEvery { sessionsRepo.fetchSessions() } returns sessions
        viewModel = viewModel()
        val events = mutableListOf<Unit>()
        backgroundScope.launch { viewModel.remindersOffEvents.toList(events) }
        runCurrent()
        return events
    }

    private fun kotlinx.coroutines.test.TestScope.collectRequests(sessions: MutableStateFlow<List<Session>>): List<Unit> {
        coEvery { sessionsRepo.fetchSessions() } returns sessions
        viewModel = viewModel()
        val requests = mutableListOf<Unit>()
        backgroundScope.launch { viewModel.notificationPermissionRequests.toList(requests) }
        runCurrent()
        return requests
    }

    private fun viewModel() =
        MainViewModel(
            sessionsRepo,
            clock,
            remoteFeatureToggle,
            syncDataWorkManager,
            notificationPermission,
            preferencesStore,
            testDispatcher,
        )

    private fun starred(count: Int) = (1..count).map { createSession(id = "$it", title = "S$it", isBookmarked = true) }

    private fun createSession(
        id: String,
        title: String,
        isBookmarked: Boolean = false,
        startsAt: Long = 1,
    ) = Session(
        id = id,
        description = "Description",
        sessionFormat = "Format",
        sessionLevel = "Level",
        slug = "slug-$id",
        title = title,
        endDateTime = "2023-11-17 10:00:00",
        endTime = "10:00 AM",
        isBookmarked = isBookmarked,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = "",
        startDateTime = "2023-11-17 09:00:00",
        startTime = "09:00 AM",
        rooms = "Room 1",
        speakers = listOf(),
        remoteId = id,
        eventDay = "1",
        startInstant = Instant.fromEpochMilliseconds(startsAt * 3_600_000),
    )
}