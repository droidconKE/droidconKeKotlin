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
package com.android254.presentation.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ke.droidcon.kotlin.core.notifications.NotificationChannelSpec
import ke.droidcon.kotlin.core.ui.R
import timber.log.Timber

/**
 * WorkManager worker that posts session reminder notifications.
 */
class SessionReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = appContext.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                Timber.d("POST_NOTIFICATIONS permission not granted; skipping notification")
                return Result.success()
            }
        }

        val sessionId = inputData.getString(KEY_SESSION_ID)
            ?: return Result.failure().also { Timber.e("Missing session ID") }
        val title = inputData.getString(KEY_TITLE)
            ?: return Result.failure().also { Timber.e("Missing session title") }
        val room = inputData.getString(KEY_ROOM).orEmpty()
        val speakers = inputData.getString(KEY_SPEAKERS).orEmpty()

        val deepLink = Uri.parse("droidconke://session/$sessionId")
        val contentIntent = android.app.PendingIntent.getActivity(
            appContext,
            sessionId.hashCode(),
            Intent(Intent.ACTION_VIEW, deepLink).setPackage(appContext.packageName),
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(
            appContext,
            NotificationChannelSpec.SessionReminders.id,
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(appContext.getString(R.string.reminder_title, title))
            .setContentText(appContext.getString(R.string.reminder_body, room, speakers))
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    appContext.getString(R.string.reminder_big_text, title, room, speakers),
                ),
            )
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .build()

        NotificationManagerCompat.from(appContext)
            .notify(sessionId.hashCode(), notification)

        Timber.d("Posted reminder notification for session $sessionId")
        Result.success()
    } catch (e: Exception) {
        Timber.e(e, "Error posting session reminder")
        Result.retry()
    }

    companion object {
        const val KEY_SESSION_ID = "session_id"
        const val KEY_TITLE = "title"
        const val KEY_ROOM = "room"
        const val KEY_SPEAKERS = "speakers"
    }
}
