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
package com.android254.data.work

import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.android254.data.work.WorkConstants.SYNC_DATA_WORKER_NAME
import com.android254.domain.work.SyncDataWorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncDataWorkManagerImpl
    @Inject
    constructor(
        private val workManager: WorkManager,
    ) : SyncDataWorkManager {
        override val isSyncing: Flow<Boolean> =
            workManager
                .getWorkInfosForUniqueWorkLiveData(SYNC_DATA_WORKER_NAME)
                .map(List<WorkInfo>::anyRunning)
                .asFlow()
                .conflate()

        override fun startSync() {
            val syncDataRequest =
                OneTimeWorkRequestBuilder<SyncDataWorker>()
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build(),
                    ).build()
            workManager.enqueueUniqueWork(SYNC_DATA_WORKER_NAME, ExistingWorkPolicy.KEEP, syncDataRequest)
        }

        override fun setupPeriodicSync() {
            val syncDataRequest =
                PeriodicWorkRequestBuilder<SyncDataWorker>(24, TimeUnit.HOURS)
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build(),
                    ).build()
            workManager.enqueueUniquePeriodicWork(
                SYNC_DATA_WORKER_NAME + "_periodic",
                ExistingPeriodicWorkPolicy.KEEP,
                syncDataRequest,
            )
        }
    }

val List<WorkInfo>.anyRunning get() = any { it.state == WorkInfo.State.RUNNING }