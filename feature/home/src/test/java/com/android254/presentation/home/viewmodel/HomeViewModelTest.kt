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
package com.android254.presentation.home.viewmodel

import com.android254.domain.models.Home
import com.android254.domain.repos.HomeRepo
import com.android254.presentation.home.viewstate.HomeHero
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import ke.droidcon.kotlin.core.testing.FakeFeatureFlags
import ke.droidcon.kotlin.core.testing.FakeSyncWorkManager
import ke.droidcon.kotlin.core.testing.testSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

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

            val viewModel = HomeViewModel(homeRepo, syncDataWorkManager, clock, FakeFeatureFlags(), TimeZone.UTC, testDispatcher)

            val job = launch { viewModel.viewState.collect() }
            runCurrent()

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
            val viewModel = HomeViewModel(homeRepo, syncDataWorkManagerMock, clock, FakeFeatureFlags(), TimeZone.UTC, testDispatcher)

            viewModel.startRefresh()
            advanceUntilIdle()

            coVerify { syncDataWorkManagerMock.startSync() }
        }

    @Test
    fun `the hero counts down as the clock ticks over midnight, with no new data`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            var now = Instant.parse("2026-11-04T23:59:30+03:00")
            val clock =
                object : Clock {
                    override fun now() = now
                }
            val keynote = testSession("keynote", Instant.parse("2026-11-06T09:00:00+03:00"), Instant.parse("2026-11-06T10:00:00+03:00"))
            coEvery { homeRepo.fetchHomeDetails() } returns flowOf(Home(sessions = listOf(keynote)))

            val viewModel = HomeViewModel(homeRepo, syncDataWorkManager, clock, FakeFeatureFlags(), TimeZone.of("Africa/Nairobi"), testDispatcher)
            val job = launch { viewModel.viewState.collect() }
            runCurrent()
            assertThat(viewModel.viewState.value.hero, `is`(HomeHero.Countdown(daysUntil = 2) as HomeHero))

            now = Instant.parse("2026-11-05T00:00:31+03:00")
            advanceTimeBy(60_001)
            runCurrent()
            assertThat(viewModel.viewState.value.hero, `is`(HomeHero.Countdown(daysUntil = 1) as HomeHero))
            job.cancel()
        }

    @Test
    fun `the cube follows its remote switch`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            Dispatchers.setMain(testDispatcher)
            coEvery { homeRepo.fetchHomeDetails() } returns flowOf(Home())

            for (enabled in listOf(true, false)) {
                val flags = FakeFeatureFlags(isCubeHeroEnabled = enabled)
                val viewModel = HomeViewModel(homeRepo, syncDataWorkManager, mockk(relaxed = true), flags, TimeZone.UTC, testDispatcher)
                val job = launch { viewModel.viewState.collect() }
                runCurrent()

                assertThat(viewModel.viewState.value.showCube, `is`(enabled))
                job.cancel()
            }
        }
}