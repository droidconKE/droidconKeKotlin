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
package com.android254.presentation.home.summary

import com.android254.domain.repos.SessionsRepo
import io.mockk.every
import io.mockk.mockk
import ke.droidcon.kotlin.core.testing.FakeFeatureFlags
import ke.droidcon.kotlin.core.testing.testSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StarredSummaryViewModelTest {
    private val repo = mockk<SessionsRepo>()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.vm(
        enabled: Boolean,
        result: SummaryResult,
    ): StarredSummaryViewModel {
        every { repo.fetchSessions() } returns flowOf(listOf(testSession("a", starred = true), testSession("b")))
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        return StarredSummaryViewModel(repo, FakeFeatureFlags(starredSummary = enabled), { result }, dispatcher)
    }

    @Test
    fun `is hidden while the switch is off`() =
        runTest {
            val vm = vm(false, SummaryResult.Unavailable)
            val job = launch { vm.state.collect() }
            runCurrent()
            assertEquals(StarredSummaryState.Hidden, vm.state.value)
            job.cancel()
        }

    @Test
    fun `says so when the phone cannot run Gemini Nano`() =
        runTest {
            val vm = vm(true, SummaryResult.Unavailable)
            val job = launch { vm.state.collect() }
            runCurrent()
            assertEquals(StarredSummaryState.Unavailable, vm.state.value)
            job.cancel()
        }

    @Test
    fun `shows the summary when generation succeeds`() =
        runTest {
            val summary = StarredSummary("Compose talks", listOf("Compose"))
            val vm = vm(true, SummaryResult.Success(summary))
            val job = launch { vm.state.collect() }
            runCurrent()
            assertEquals(StarredSummaryState.Ready(summary), vm.state.value)
            job.cancel()
        }
}