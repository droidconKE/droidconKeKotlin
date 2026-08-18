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

import com.android254.domain.repos.OrganizersRepo
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.SpeakersRepo
import com.android254.domain.repos.SponsorsRepo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeRepoImplTest {
    private val speakersRepo = mockk<SpeakersRepo>()
    private val sessionsRepo = mockk<SessionsRepo>()
    private val sponsorsRepo = mockk<SponsorsRepo>()
    private val organizersRepo = mockk<OrganizersRepo>()

    @Test
    fun `fetchHomeDetails should combine data from all repos`() =
        runTest {
            coEvery { speakersRepo.fetchSpeakers() } returns flowOf(emptyList())
            coEvery { sessionsRepo.fetchSessions() } returns flowOf(emptyList())
            coEvery { sponsorsRepo.getAllSponsors() } returns flowOf(emptyList())
            coEvery { organizersRepo.getOrganizers() } returns flowOf(emptyList())

            val homeRepo =
                HomeRepoImpl(
                    speakersRepo,
                    sessionsRepo,
                    sponsorsRepo,
                    organizersRepo,
                    UnconfinedTestDispatcher(testScheduler),
                )

            val homeDetails = homeRepo.fetchHomeDetails().first()

            assertThat(homeDetails.speakers.isEmpty(), `is`(true))
            assertThat(homeDetails.sessions.isEmpty(), `is`(true))
            assertThat(homeDetails.sponsors.isEmpty(), `is`(true))
            assertThat(homeDetails.organizerLogos.isEmpty(), `is`(true))
        }
}