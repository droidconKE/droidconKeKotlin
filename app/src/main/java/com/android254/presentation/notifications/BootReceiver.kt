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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android254.domain.repos.SessionsRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Broadcast receiver that reschedules session reminders after device boot or app update.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var sessionsRepo: SessionsRepo

    @Inject
    lateinit var sessionReminderScheduler: SessionReminderScheduler

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        Timber.d("BootReceiver: ${intent.action}, rescheduling reminders")

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val allSessions = sessionsRepo.fetchSessions().first()
                val starred = allSessions.filter { it.isBookmarked }

                sessionReminderScheduler.rescheduleAll(starred)

                Timber.d("Rescheduled ${starred.size} reminders after boot")
            } catch (e: Exception) {
                Timber.e(e, "Error rescheduling reminders after boot")
            }
        }
    }
}