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
package com.android254.droidcon.app

import android.app.Application
import com.android254.domain.repos.SessionsRepo
import com.android254.domain.work.SyncDataWorkManager
import com.android254.droidcon.crashlytics.CrashlyticsTree
import com.android254.presentation.notifications.SessionReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import ke.droidcon.kotlin.BuildConfig
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class DroidconApp : Application() {
    @Inject
    lateinit var syncDataWorkManager: SyncDataWorkManager

    @Inject
    lateinit var sessionsRepository: SessionsRepo

    @Inject
    lateinit var sessionReminderScheduler: SessionReminderScheduler

    override fun onCreate() {
        super.onCreate()
        initTimber()
        NotificationChannelSpec.createAll(this)

        syncDataWorkManager.setupPeriodicSync()

        syncDataWorkManager.startSync()

        // Reschedule reminders whenever starred sessions change
        sessionsRepository
            .fetchSessions()
            .map { sessions -> sessions.filter { it.isBookmarked } }
            .distinctUntilChanged()
            .onEach { starredSessions ->
                sessionReminderScheduler.rescheduleAll(starredSessions)
            }.launchIn(CoroutineScope(Dispatchers.Default + Job()))
    }

    private fun initTimber() =
        when {
            BuildConfig.DEBUG -> {
                Timber.plant(
                    object : Timber.DebugTree() {
                        override fun createStackElementTag(element: StackTraceElement): String = super.createStackElementTag(element) + ":" + element.lineNumber
                    },
                )
            }

            else -> {
                Timber.plant(CrashlyticsTree())
            }
        }
}