/*
 * Copyright 2023 DroidconKE
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

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import ke.droidcon.kotlin.datasource.local.source.LocalSponsorsDataSource
import ke.droidcon.kotlin.datasource.remote.sponsors.RemoteSponsorsDataSource
import ke.droidcon.kotlin.datasource.remote.sponsors.model.SponsorDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SponsorsManagerTest {
    private val mockLocalSponsorsDataSource = mockk<LocalSponsorsDataSource>()
    private val mockRemoteSponsorsDataSource = mockk<RemoteSponsorsDataSource>()

    @Test
    fun `test syncWith reconciles data`() =
        runTest {
            val remoteSponsor = mockk<SponsorDTO>()
            every { remoteSponsor.name } returns "Sponsor 1"
            coEvery { mockRemoteSponsorsDataSource.getAllSponsorsRemote() } returns DataResult.Success(listOf(remoteSponsor))
            coEvery { mockLocalSponsorsDataSource.getNames() } returns listOf("Sponsor 1", "Sponsor 2")
            coEvery { mockLocalSponsorsDataSource.saveCachedSponsors(any()) } returns Unit
            coEvery { mockLocalSponsorsDataSource.deleteByNames(any()) } returns Unit

            val manager = SponsorsManager(mockLocalSponsorsDataSource, mockRemoteSponsorsDataSource)
            val result = manager.syncWith(mockk())

            assert(result)
            coVerify { mockLocalSponsorsDataSource.deleteByNames(listOf("Sponsor 2")) }
            coVerify { mockLocalSponsorsDataSource.saveCachedSponsors(any()) }
        }
}