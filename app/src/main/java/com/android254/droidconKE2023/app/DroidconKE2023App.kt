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
package com.android254.droidconKE2023.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.android254.data.work.WorkConstants
import com.android254.data.work.WorkInitializer
import com.android254.droidconKE2023.crashlytics.CrashlyticsTree
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import ke.droidcon.kotlin.BuildConfig
import ke.droidcon.kotlin.datasource.remote.utils.RemoteFeatureToggle
import org.jetbrains.annotations.NotNull
import timber.log.Timber

@HiltAndroidApp
class DroidconKE2023App : Application(), Configuration.Provider {
    @Inject
    lateinit var remoteFeatureToggle: RemoteFeatureToggle

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override fun onCreate() {
        super.onCreate()
        remoteFeatureToggle.sync()
        initTimber()
        setUpWorkerManagerNotificationChannel()
        WorkInitializer.initialize(context = this)
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .setWorkerFactory(workerFactory)
            .build()

    private fun initTimber() = when {
        BuildConfig.DEBUG -> {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(@NotNull element: StackTraceElement): String {
                    return super.createStackElementTag(element) + ":" + element.lineNumber
                }
            })
        }

        else -> {
            Timber.plant(CrashlyticsTree())
        }
    }

    private fun setUpWorkerManagerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WorkConstants.NOTIFICATION_CHANNEL,
                WorkConstants.syncDataWorkerName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        WorkManager.initialize(
            this,
            Configuration.Builder().setWorkerFactory(workerFactory).build()
        )
    }
}