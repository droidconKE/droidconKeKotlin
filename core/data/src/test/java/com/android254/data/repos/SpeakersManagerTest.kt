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

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import ke.droidcon.kotlin.datasource.local.source.LocalSpeakersDataSource
import ke.droidcon.kotlin.datasource.remote.speakers.RemoteSpeakersDataSource
import ke.droidcon.kotlin.datasource.remote.speakers.model.SpeakerDTO
import ke.droidcon.kotlin.datasource.remote.utils.DataResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class SpeakersManagerTest {
    private val mockLocalSpeakersDataSource = mockk<LocalSpeakersDataSource>()
    private val mockRemoteSpeakersDataSource = mockk<RemoteSpeakersDataSource>()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `test syncWith reconciles data`() =
        runTest {
            val remoteSpeaker = mockk<SpeakerDTO>(relaxed = true)
            every { remoteSpeaker.name } returns "Speaker 1"
            coEvery { mockRemoteSpeakersDataSource.getAllSpeakersRemote() } returns DataResult.Success(listOf(remoteSpeaker))
            coEvery { mockLocalSpeakersDataSource.getNames() } returns listOf("Speaker 1", "Speaker 2")
            coEvery { mockLocalSpeakersDataSource.saveCachedSpeakers(any()) } returns Unit
            coEvery { mockLocalSpeakersDataSource.deleteByNames(any()) } returns Unit

            val manager = SpeakersManager(mockLocalSpeakersDataSource, mockRemoteSpeakersDataSource, testDispatcher)
            val result = manager.syncWith(mockk())

            assert(result)
            coVerify { mockLocalSpeakersDataSource.deleteByNames(listOf("Speaker 2")) }
            coVerify { mockLocalSpeakersDataSource.saveCachedSpeakers(any()) }
        }
}