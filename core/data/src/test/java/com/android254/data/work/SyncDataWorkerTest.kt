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
package com.android254.data.work

import androidx.work.ListenableWorker
import com.android254.domain.repos.FeedRepo
import com.android254.domain.repos.OrganizersRepo
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.SpeakersRepo
import com.android254.domain.repos.SponsorsRepo
import com.android254.domain.sync.Syncable
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import ke.droidcon.kotlin.datasource.local.SyncTransaction
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncDataWorkerTest {
    private val sessions = syncing<SessionsRepo>()
    private val speakers = syncing<SpeakersRepo>()
    private val sponsors = syncing<SponsorsRepo>()
    private val organizers = syncing<OrganizersRepo>()
    private val feed = syncing<FeedRepo>()
    private val toggle = mockk<RemoteFeatureToggle>()

    @Test
    fun `a failed Remote Config fetch still syncs the data`() =
        runTest {
            coEvery { toggle.syncNowIfEmpty() } throws IllegalStateException("offline")

            val result = worker(StandardTestDispatcher(testScheduler)).doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            listOf(sessions, speakers, sponsors, organizers, feed).forEach { coVerify(exactly = 1) { it.syncWith(any()) } }
        }

    @Test
    fun `a repository that fails to sync asks for a retry`() =
        runTest {
            coEvery { toggle.syncNowIfEmpty() } returns false
            coEvery { feed.syncWith(any()) } returns false

            assertEquals(ListenableWorker.Result.retry(), worker(StandardTestDispatcher(testScheduler)).doWork())
        }

    private fun worker(dispatcher: kotlinx.coroutines.CoroutineDispatcher) =
        SyncDataWorker(
            appContext = mockk(relaxed = true),
            workerParameters = mockk(relaxed = true),
            ioDispatcher = dispatcher,
            speakersRepo = speakers,
            sponsorsRepo = sponsors,
            sessionsRepo = sessions,
            organizersRepo = organizers,
            feedRepo = feed,
            remoteFeatureToggle = toggle,
            syncTransaction = mockk<SyncTransaction>(relaxed = true),
            widgetRefresher = mockk(relaxed = true),
        )

    private inline fun <reified T : Syncable> syncing(): T = mockk<T>(relaxed = true).also { coEvery { it.syncWith(any()) } returns true }
}