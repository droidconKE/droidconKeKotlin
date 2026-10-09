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

import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.android254.data.work.WorkConstants.PERIODIC_SYNC_DATA_WORKER_NAME
import com.android254.data.work.WorkConstants.SYNC_DATA_WORKER_NAME
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.util.UUID

class SyncDataWorkManagerImplTest {
    private val workManager = mockk<WorkManager>()

    private fun work(state: WorkInfo.State) = WorkInfo(UUID.randomUUID(), state, emptySet())

    private suspend fun isSyncing(
        oneTime: List<WorkInfo>,
        periodic: List<WorkInfo>,
    ): Boolean {
        every { workManager.getWorkInfosForUniqueWorkFlow(SYNC_DATA_WORKER_NAME) } returns flowOf(oneTime)
        every { workManager.getWorkInfosForUniqueWorkFlow(PERIODIC_SYNC_DATA_WORKER_NAME) } returns flowOf(periodic)
        return SyncDataWorkManagerImpl(workManager).isSyncing.first()
    }

    @Test
    fun `the daily sync running counts as syncing`() =
        runTest {
            assertThat(isSyncing(oneTime = emptyList(), periodic = listOf(work(WorkInfo.State.RUNNING))), `is`(true))
        }

    @Test
    fun `a running one-time sync counts as syncing`() =
        runTest {
            assertThat(isSyncing(oneTime = listOf(work(WorkInfo.State.RUNNING)), periodic = emptyList()), `is`(true))
        }

    @Test
    fun `queued work does not count as syncing`() =
        runTest {
            assertThat(
                isSyncing(oneTime = listOf(work(WorkInfo.State.ENQUEUED)), periodic = listOf(work(WorkInfo.State.ENQUEUED))),
                `is`(false),
            )
        }
}