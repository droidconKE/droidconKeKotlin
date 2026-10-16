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

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.android254.domain.models.Session
import ke.droidcon.kotlin.core.notifications.preferences.NotificationPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/**
 * Orchestrates WorkManager scheduling for session reminders.
 */
@Singleton
class SessionReminderScheduler
    @Inject
    constructor(
        private val workManager: WorkManager,
        private val preferencesStore: NotificationPreferencesStore,
        private val clock: Clock,
    ) {
        suspend fun rescheduleAll(starredSessions: List<Session>) {
            workManager.cancelAllWorkByTag(TAG_SESSION_REMINDER)

            val prefs = preferencesStore.preferences.first()

            if (!prefs.sessionRemindersEnabled) {
                Timber.d("Session reminders disabled; not scheduling")
                return
            }

            val leadTime = prefs.reminderLeadTimeMinutes.minutes
            val now = clock.now()

            starredSessions
                .mapNotNull { session ->
                    val startTime = session.startInstant ?: return@mapNotNull null
                    if (startTime > now) session else null
                }.forEach { session ->
                    val fireAt = session.startInstant!! - leadTime

                    if (fireAt > now) {
                        val delay = fireAt - now

                        Timber.d(
                            "Scheduling reminder for session ${session.remoteId} at $fireAt " +
                                "(delay: $delay)",
                        )

                        workManager.enqueueUniqueWork(
                            "reminder_${session.remoteId}",
                            ExistingWorkPolicy.REPLACE,
                            OneTimeWorkRequestBuilder<SessionReminderWorker>()
                                .setInitialDelay(delay.toJavaDuration())
                                .addTag(TAG_SESSION_REMINDER)
                                .setInputData(
                                    workDataOf(
                                        SessionReminderWorker.KEY_SESSION_ID to session.remoteId,
                                        SessionReminderWorker.KEY_TITLE to session.title,
                                        SessionReminderWorker.KEY_ROOM to session.rooms,
                                        SessionReminderWorker.KEY_SPEAKERS to session.speakers.joinToString { it.name },
                                    ),
                                ).build(),
                        )
                    } else {
                        Timber.d("Reminder time for session ${session.remoteId} is in the past; skipping")
                    }
                }

            Timber.d("Scheduled ${starredSessions.size} reminders")
        }

        companion object {
            private const val TAG_SESSION_REMINDER = "session_reminder"
        }
    }