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
import ke.droidcon.kotlin.datasource.local.source.LocalOrganizersDataSource
import ke.droidcon.kotlin.datasource.remote.organizers.RemoteOrganizersDataSource
import ke.droidcon.kotlin.datasource.remote.organizers.model.OrganizerDTO
import ke.droidcon.kotlin.datasource.remote.organizers.model.OrganizersPagedResponse
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class OrganizersManagerTest {
    private val mockLocalOrganizersDataSource = mockk<LocalOrganizersDataSource>()
    private val mockRemoteOrganizersDataSource = mockk<RemoteOrganizersDataSource>()

    @Test
    fun `test syncWith reconciles data`() =
        runTest {
            val remoteOrganizer = mockk<OrganizerDTO>()
            every { remoteOrganizer.name } returns "Organizer 1"
            coEvery { mockRemoteOrganizersDataSource.getIndividualOrganizers() } returns DataResult.Success(OrganizersPagedResponse(listOf(remoteOrganizer)))
            coEvery { mockRemoteOrganizersDataSource.getCompanyOrganizers() } returns DataResult.Success(OrganizersPagedResponse(emptyList()))
            coEvery { mockLocalOrganizersDataSource.getNames() } returns listOf("Organizer 1", "Organizer 2")
            coEvery { mockLocalOrganizersDataSource.insertOrganizers(any()) } returns Unit
            coEvery { mockLocalOrganizersDataSource.deleteByNames(any()) } returns Unit

            val manager = OrganizersManager(mockLocalOrganizersDataSource, mockRemoteOrganizersDataSource)
            val result = manager.syncWith(mockk())

            assert(result)
            coVerify { mockLocalOrganizersDataSource.deleteByNames(listOf("Organizer 2")) }
            coVerify { mockLocalOrganizersDataSource.insertOrganizers(any()) }
        }
}