package com.android254.data.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.android254.data.work.WorkConstants.syncDataWorkerName

object WorkInitializer {
    fun initialize(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(
                        NetworkType.CONNECTED
                    )
                    .build())
            .build()
        WorkManager.getInstance(context).apply {
            enqueueUniqueWork(syncDataWorkerName,
                ExistingWorkPolicy.KEEP,request)
        }
    }
}