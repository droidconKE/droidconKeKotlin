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
package com.android254.droidcon.appfunctions

import androidx.appfunctions.AppFunctionDisabledException
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.test.filters.SdkSuppress
import com.android254.domain.flags.FeatureFlags
import com.android254.domain.models.Session
import com.android254.domain.repos.SessionsRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@SdkSuppress(minSdkVersion = 36)
class StarSessionAppFunctionServiceTest {
    private val repo = mockk<SessionsRepo>(relaxed = true)
    private val flags = mockk<FeatureFlags> { coEvery { isStarSessionFunctionEnabled() } returns true }
    private val service =
        StarSessionAppFunctionService().also {
            it.sessionsRepo = repo
            it.featureFlags = flags
        }

    @Test
    fun `starring is refused while the Remote Config switch is off`() =
        runTest {
            coEvery { flags.isStarSessionFunctionEnabled() } returns false

            val failure = runCatching { service.starSession("1112") }.exceptionOrNull()

            assertEquals(AppFunctionDisabledException::class, failure?.let { it::class })
            coVerify(exactly = 0) { repo.bookmarkSession(any()) }
        }

    @Test
    fun `starring a known session bookmarks it and returns its title`() =
        runTest {
            every { repo.fetchSessionById("1112") } returns flowOf(session("1112", "Welcome"))

            val result = service.starSession("1112")

            assertEquals(StarredSession("1112", "Welcome"), result)
            coVerify(exactly = 1) { repo.bookmarkSession("1112") }
        }

    @Test
    fun `starring an unknown session fails and bookmarks nothing`() =
        runTest {
            every { repo.fetchSessionById("nope") } returns flowOf(null)

            val failure = runCatching { service.starSession("nope") }.exceptionOrNull()

            assertEquals(AppFunctionElementNotFoundException::class, failure?.let { it::class })
            coVerify(exactly = 0) { repo.bookmarkSession(any()) }
        }

    @Test
    fun `a blank id is rejected`() =
        runTest {
            val failure = runCatching { service.starSession(" ") }.exceptionOrNull()

            assertEquals(AppFunctionInvalidArgumentException::class, failure?.let { it::class })
            coVerify(exactly = 0) { repo.bookmarkSession(any()) }
        }

    private fun session(
        id: String,
        title: String,
    ) = Session(
        id = id,
        endDateTime = "",
        endTime = "",
        isBookmarked = false,
        isKeynote = false,
        isServiceSession = false,
        sessionImage = null,
        startDateTime = "",
        startTime = "",
        rooms = "",
        speakers = emptyList(),
        remoteId = id,
        description = "",
        sessionFormat = "",
        sessionLevel = "",
        slug = "",
        title = title,
        eventDay = "",
    )
}