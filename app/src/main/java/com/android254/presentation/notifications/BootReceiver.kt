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
import dagger.hilt.android.AndroidEntryPoint
import ke.droidcon.kotlin.core.domain.repository.SessionsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Broadcast receiver that reschedules session reminders after device boot or app update.
 *
 * Listens for:
 * - BOOT_COMPLETED: Device reboot
 * - MY_PACKAGE_REPLACED: App update/reinstall
 *
 * On these events, it:
 * 1. Fetches all starred sessions
 * 2. Calls [SessionReminderScheduler.rescheduleAll] to re-establish reminders
 *
 * This ensures reminders survive device reboots and app updates.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var sessionsRepository: SessionsRepository

    @Inject
    lateinit var sessionReminderScheduler: SessionReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        // Only handle boot and update intents
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        Timber.d("BootReceiver: ${intent.action}, rescheduling reminders")

        // Launch in a coroutine to avoid ANR (Application Not Responding)
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Fetch all starred sessions
                val sessions = sessionsRepository.fetchSessions()
                val starred = sessions.filter { it.isBookmarked }

                // Reschedule reminders
                sessionReminderScheduler.rescheduleAll(starred)

                Timber.d("Rescheduled ${starred.size} reminders after boot")
            } catch (e: Exception) {
                Timber.e(e, "Error rescheduling reminders after boot")
            }
        }
    }
}
