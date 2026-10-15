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
import dagger.hilt.android.scopes.ViewModelScoped
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
 *
 * Manages the lifecycle of reminder work requests, ensuring that:
 * - Only future sessions are scheduled
 * - Reminders respect user preferences (can be disabled)
 * - Existing reminders are cancelled before rescheduling
 * - Reminders survive device reboot and app updates
 */
@Singleton
class SessionReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val preferencesStore: NotificationPreferencesStore,
    private val clock: Clock,
) {
    /**
     * Cancels all existing session reminders and schedules new ones for all future starred sessions.
     *
     * This method:
     * 1. Cancels all work tagged with "session_reminder"
     * 2. Reads user preferences to check if reminders are enabled
     * 3. Gets the configured reminder lead time
     * 4. For each starred session, if it hasn't started yet:
     *    - Calculates the fire time (startInstant - leadTime)
     *    - If the fire time is in the future, enqueues a work request
     *    - Uses a unique ID per session to allow re-scheduling
     *
     * Called when:
     * - Starred sessions change (bookmark/unbookmark)
     * - Sync completes (session times may have changed)
     * - Device boots (via BootReceiver)
     * - Preferences change (reminders toggle)
     *
     * @param starredSessions List of sessions the user has bookmarked
     */
    suspend fun rescheduleAll(starredSessions: List<Session>) {
        // Cancel all existing reminders first
        workManager.cancelAllWorkByTag(TAG_SESSION_REMINDER)

        // Read preferences
        val prefs = preferencesStore.preferences.first()

        // If reminders are disabled, don't schedule any
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
            }
            .forEach { session ->
                val fireAt = session.startInstant!!.minus(leadTime)

                // Only schedule if the reminder time is still in the future
                if (fireAt > now) {
                    val delay = fireAt - now

                    Timber.d(
                        "Scheduling reminder for session ${session.remoteId} at $fireAt " +
                            "(delay: $delay)"
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
                            )
                            .build(),
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
