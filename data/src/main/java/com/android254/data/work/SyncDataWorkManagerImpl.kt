package com.android254.data.work

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.lifecycle.map
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.android254.domain.work.SyncDataWorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import javax.inject.Inject




class SyncDataWorkManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
): SyncDataWorkManager {


    override val isSyncing: Flow<Boolean> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(syncDataWorkerName)
            .map(MutableList<WorkInfo>::anyRunning)
            .asFlow()
            .conflate()

    override suspend fun startSync() {
        val syncDataRequest = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        val workManager = WorkManager.getInstance(context)
        workManager.beginUniqueWork(syncDataWorkerName, ExistingWorkPolicy.KEEP, syncDataRequest)
            .enqueue()
    }

    companion object {
        const val syncDataWorkerName = "sync_data"
    }
}

val List<WorkInfo>.anyRunning get() = any { it.state == WorkInfo.State.RUNNING }