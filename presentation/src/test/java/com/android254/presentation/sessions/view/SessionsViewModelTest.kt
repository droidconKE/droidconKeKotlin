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
package com.android254.presentation.sessions.view

import com.android254.domain.models.Session
import com.android254.domain.models.SessionsInformationDomainModel
import com.android254.domain.repos.SessionsRepo
import com.android254.presentation.models.EventDate
import com.android254.presentation.models.SessionsFilterOption
import com.android254.presentation.sessions.models.SessionsIntentHandler
import com.android254.presentation.sessions.utils.SessionsFilterCategory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModelTest {
    private val sessionsRepo = mockk<SessionsRepo>(relaxed = true)
    private val syncDataWorkManager = FakeSyncWorkManager()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should set initial selected date to current date if it exists in event days`() = runTest {
        val currentDay = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
        val eventDays = listOf(currentDay, "17", "18")
        coEvery { sessionsRepo.fetchSessionsInformation() } returns flowOf(
            SessionsInformationDomainModel(
                sessions = emptyList(),
                eventDays = eventDays
            )
        )

        val viewModel = SessionsViewModel(sessionsRepo, syncDataWorkManager)
        
        val job = launch { viewModel.sessionsUiState.collect() }
        advanceUntilIdle()
        
        assertThat(viewModel.selectedEventDay.value.value, `is`(currentDay))
        job.cancel()
    }

    @Test
    fun `should set initial selected date to first event day if current date is not an event day`() = runTest {
        val eventDays = listOf("10", "11", "12")
        coEvery { sessionsRepo.fetchSessionsInformation() } returns flowOf(
            SessionsInformationDomainModel(
                sessions = emptyList(),
                eventDays = eventDays
            )
        )

        val viewModel = SessionsViewModel(sessionsRepo, syncDataWorkManager)
        
        val job = launch { viewModel.sessionsUiState.collect() }
        advanceUntilIdle()
        
        assertThat(viewModel.selectedEventDay.value.value, `is`("10"))
        job.cancel()
    }

    @Test
    fun `should update selected day when UpdateSelectedDay intent is handled`() = runTest {
        val viewModel = SessionsViewModel(sessionsRepo, syncDataWorkManager)
        val newDay = EventDate("17", 2)
        
        viewModel.handleEvent(SessionsIntentHandler.UpdateSelectedDay(newDay))
        
        assertThat(viewModel.selectedEventDay.value, `is`(newDay))
    }

    @Test
    fun `should call bookmarkSession in repo when BookmarkSession intent is handled`() = runTest {
        val viewModel = SessionsViewModel(sessionsRepo, syncDataWorkManager)
        val sessionId = "session_id"
        
        viewModel.bookmarkSession(sessionId)
        advanceUntilIdle()
        
        coVerify { sessionsRepo.bookmarkSession(sessionId) }
    }

    @Test
    fun `should call unBookmarkSession in repo when unBookmarkSession is called`() = runTest {
        val viewModel = SessionsViewModel(sessionsRepo, syncDataWorkManager)
        val sessionId = "session_id"
        
        viewModel.unBookmarkSession(sessionId)
        advanceUntilIdle()
        
        coVerify { sessionsRepo.unBookmarkSession(sessionId) }
    }

    @Test
    fun `should toggle bookmark filter when ToggleBookmarkFilter intent is handled`() = runTest {
        val viewModel = SessionsViewModel(sessionsRepo, syncDataWorkManager)
        assertThat(viewModel.filterState.value.isBookmarked, `is`(false))
        
        viewModel.handleEvent(SessionsIntentHandler.ToggleBookmarkFilter)
        assertThat(viewModel.filterState.value.isBookmarked, `is`(true))
        
        viewModel.handleEvent(SessionsIntentHandler.ToggleBookmarkFilter)
        assertThat(viewModel.filterState.value.isBookmarked, `is`(false))
    }

    @Test
    fun `should clear filters when ClearSelectedFilterList intent is handled`() = runTest {
        val viewModel = SessionsViewModel(sessionsRepo, syncDataWorkManager)
        val filterOption = SessionsFilterOption("Beginner", "beginner", SessionsFilterCategory.Level)
        
        viewModel.updateSelectedFilterOptionList(filterOption)
        assertThat(viewModel.selectedFilterOptions.value.size, `is`(1))
        
        viewModel.handleEvent(SessionsIntentHandler.ClearSelectedFilterList)
        
        assertThat(viewModel.selectedFilterOptions.value.isEmpty(), `is`(true))
        assertThat(viewModel.filterState.value.levels.isEmpty(), `is`(true))
    }

    @Test
    fun `should update filter state when updateSelectedFilterOptionList is called`() = runTest {
        val viewModel = SessionsViewModel(sessionsRepo, syncDataWorkManager)
        val levelFilter = SessionsFilterOption("Beginner", "Beginner", SessionsFilterCategory.Level)
        val roomFilter = SessionsFilterOption("Room 1", "Room 1", SessionsFilterCategory.Room)
        
        viewModel.updateSelectedFilterOptionList(levelFilter)
        viewModel.updateSelectedFilterOptionList(roomFilter)
        
        assertThat(viewModel.filterState.value.levels.contains("Beginner"), `is`(true))
        assertThat(viewModel.filterState.value.rooms.contains("Room 1"), `is`(true))
        assertThat(viewModel.selectedFilterOptions.value.size, `is`(2))
    }
}
