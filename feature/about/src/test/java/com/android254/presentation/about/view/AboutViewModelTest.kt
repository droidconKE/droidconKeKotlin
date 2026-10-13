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
package com.android254.presentation.about.view

import com.android254.domain.repos.OrganizersRepo
import io.mockk.every
import io.mockk.mockk
import ke.droidcon.kotlin.core.testing.FakeFeatureFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class AboutViewModelTest {
    private val organizersRepo = mockk<OrganizersRepo> { every { getOrganizers() } returns flowOf(emptyList()) }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `the cube follows its remote switch`() =
        runTest {
            val dispatcher = UnconfinedTestDispatcher(testScheduler)
            Dispatchers.setMain(dispatcher)

            listOf(true, false).forEach { enabled ->
                val viewModel = AboutViewModel(organizersRepo, FakeFeatureFlags(cubeHero = enabled), dispatcher)
                val success = viewModel.uiState.filterIsInstance<AboutScreenUiState.Success>().first()

                assertEquals(enabled, success.showCube)
            }
        }
}