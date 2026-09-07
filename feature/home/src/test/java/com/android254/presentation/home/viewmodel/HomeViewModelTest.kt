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
package com.android254.presentation.home.viewmodel

import com.android254.domain.models.Home
import com.android254.domain.repos.HomeRepo
import com.android254.presentation.sessions.view.FakeSyncWorkManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val homeRepo = mockk<HomeRepo>(relaxed = true)
    private val syncDataWorkManager = FakeSyncWorkManager()

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should fetch home details when viewState is collected`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val clock = mockk<Clock>(relaxed = true)

            val home = Home()
            coEvery { homeRepo.fetchHomeDetails() } returns flowOf(home)

            val viewModel = HomeViewModel(homeRepo, syncDataWorkManager, clock, testDispatcher)

            val job = launch { viewModel.viewState.collect() }
            advanceUntilIdle()

            assertThat(viewModel.viewState.value.isSyncing, `is`(true)) // FakeSyncWorkManager emits true
            coVerify { homeRepo.fetchHomeDetails() }
            job.cancel()
        }

    @Test
    fun `should trigger sync when startRefresh is called`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            val clock = mockk<Clock>(relaxed = true)

            val syncDataWorkManagerMock = mockk<FakeSyncWorkManager>(relaxed = true)
            val viewModel = HomeViewModel(homeRepo, syncDataWorkManagerMock, clock, testDispatcher)

            viewModel.startRefresh()
            advanceUntilIdle()

            coVerify { syncDataWorkManagerMock.startSync() }
        }
}