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

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.android254.domain.repos.FeedRepo
import com.android254.domain.repos.OrganizersRepo
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.repos.SpeakersRepo
import com.android254.domain.repos.SponsorsRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.droidcon.kotlin.data.R
import ke.droidcon.kotlin.datasource.remote.di.IoDispatcher
import kotlin.random.Random
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

@HiltWorker
class SyncDataWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted val workerParameters: WorkerParameters,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,

    private val speakersRepo: SpeakersRepo,
    private val sponsorsRepo: SponsorsRepo,
    private val sessionsRepo: SessionsRepo,
    private val organizersRepo: OrganizersRepo,
    private val feedRepo: FeedRepo

) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            Random.nextInt(),
            NotificationCompat.Builder(appContext, WorkConstants.NOTIFICATION_CHANNEL)
                .setSmallIcon(androidx.core.R.drawable.notification_bg_low)
                .setContentTitle(appContext.getString(R.string.sync_notification_message))
                .build()

        )
    }

    override suspend fun doWork(): Result {
        withContext(ioDispatcher) {
            awaitAll(
                async { sessionsRepo.syncSessions() },
                async { speakersRepo.syncSpeakers() },
                async { sponsorsRepo.syncSponsors() },
                async { organizersRepo.syncOrganizers() },
                async { feedRepo.syncFeed() }
            )
        }
        return Result.success()
    }
}